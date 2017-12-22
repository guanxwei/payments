package org.wgx.payments.execution;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.wgx.payments.builder.ActionRecordBuilder;
import org.wgx.payments.builder.PaymentResponseBuilder;
import org.wgx.payments.callback.Callback;
import org.wgx.payments.callback.CallbackDetail;
import org.wgx.payments.client.api.helper.BusinessProfile;
import org.wgx.payments.client.api.helper.PaymentOperation;
import org.wgx.payments.client.api.helper.PaymentOperation.Visitor;
import org.wgx.payments.client.api.helper.PaymentResponseStatus;
import org.wgx.payments.client.api.helper.ResponseStatus;
import org.wgx.payments.client.api.io.CallbackMetaInfo;
import org.wgx.payments.client.api.io.CreateOrUpdatePaymentResponseRequest;
import org.wgx.payments.client.api.io.CreateOrUpdatePaymentResponseResponse;
import org.wgx.payments.client.api.io.CreatePaymentRequest;
import org.wgx.payments.client.api.io.CreatePaymentResponse;
import org.wgx.payments.client.api.io.RefundRequest;
import org.wgx.payments.client.api.io.RefundResponse;
import org.wgx.payments.client.api.io.Request;
import org.wgx.payments.client.api.io.RescindRequest;
import org.wgx.payments.client.api.io.RescindResponse;
import org.wgx.payments.client.api.io.Response;
import org.wgx.payments.client.api.io.ScheduledPayRequest;
import org.wgx.payments.client.api.io.ScheduledPayResponse;
import org.wgx.payments.dao.ActionRecordDAO;
import org.wgx.payments.dao.FastSearchTableDAO;
import org.wgx.payments.dao.PaymentRequestDAO;
import org.wgx.payments.dao.PaymentResponseDAO;
import org.wgx.payments.dao.service.PaymentsDAOService;
import org.wgx.payments.deducer.Deducer;
import org.wgx.payments.facade.Facade;
import org.wgx.payments.model.ActionRecord;
import org.wgx.payments.model.ActionRecord.ErrorCode;
import org.wgx.payments.model.FastSearchTableItem;
import org.wgx.payments.model.PaymentRequest;
import org.wgx.payments.model.PaymentRequestStatus;
import org.wgx.payments.model.PaymentResponse;
import org.wgx.payments.signature.Account;
import org.wgx.payments.signature.AccountFactory;
import org.wgx.payments.signature.SignatureGenerator;
import org.wgx.payments.tools.Jackson;
import org.wgx.payments.utils.AccountUtils;
import org.wgx.payments.validator.Validator;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

/**
 * Abstract of simple payment processor, which is intended to provide basic support for operations: Charge and Refund operation.
 * Other operations like Sign operation will be default treat as unsupported.
 *
 * To support such operations, sub-class should override these <b> protected </b> methods based on their requirements:
 * {@link SimplePaymentProcessor#invokeSignOperation(CreatePaymentRequest)}.
 * {@link SimplePaymentProcessor#invokeRescindOperation(RescindRequest)}.
 * {@link SimplePaymentProcessor#invokeScheduledPayOperation(ScheduledPayRequest)}.
 *
 */
@Slf4j
@Data
public abstract class SimplePaymentProcessor implements PaymentProcessor {

    private PaymentRequestDAO paymentRequestDAO;

    private Validator<CreateOrUpdatePaymentResponseRequest> validator;

    private PaymentResponseDAO paymentResponseDAO;

    private ActionRecordDAO actionRecordDAO;

    private SignatureGenerator signatureGenerator;

    private Callback callback;

    private PaymentsDAOService paymentsDAOService;

    private ThreadLocal<PaymentRequest> localRequest = new ThreadLocal<>();

    private ThreadLocal<PaymentResponse> localResponse = new ThreadLocal<>();

    private ThreadLocal<String> transactionID = new ThreadLocal<>();

    private ThreadLocal<Account> account = new ThreadLocal<>();

    private Facade<Pair<String, String>, String> facade;

    private Deducer<String, BusinessProfile> businessProfileDeducer;

    private Deducer<Pair<Request, String>, Pair<String, String>> accountDeducer;

    private FastSearchTableDAO fastSearchTableDAO;

    private AccountFactory accountFactory;

    /**
     * {@inheritDoc}
     *
     */
    @Override
    public Response processRequest(final Request request) {

        PaymentOperation paymentOperation = PaymentOperation.fromString(request.getPaymentOperationType());
        if (paymentOperation == null) {
            return logAndReturn(request, REQUEST_TO_RESPONSE_MAPPING.get(request.getClass()));
        }

        log.info("Operation [{}] deduced to be executed on incoming request [{}]",
                paymentOperation.operationType(), Jackson.json(request));

        BusinessProfile businessProfile = getBusinessProfileDeducer().deduce(request.getBusiness());
        log.info("Deduced business [{}] from request, raw input is [{}]", businessProfile.profile(), request.getBusiness());

        request.setBusiness(businessProfile.profile());

        // Prepare payment account and material information.
        if (!deducePaymentAccount(request, getPaymentProcessorName())) {
            return generatePaymentAccountNotFoundResponse();
        }

        // Abstract transaction id generation procedure to common step.
        generateTransactionID(businessProfile, paymentOperation.operationType());

        return execute(paymentOperation, request);

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Response processResponse(final Request request) {
        CreateOrUpdatePaymentResponseRequest createOrUpdatePaymentResponseRequest = (CreateOrUpdatePaymentResponseRequest) request;
        log.info("Notification [{}] to be processed", Jackson.json(createOrUpdatePaymentResponseRequest));
        return handleResponse(createOrUpdatePaymentResponseRequest);
    }

    /**
     * Log error info and return error response.
     * @param request Any request incoming.
     * @param clazz The request's real type.
     * @return Error response.
     */
    protected Response logAndReturn(final Request request, final Class<?> clazz) {
        log.error(String.format("Wrong payment operation type : [%s] is specified for payment method : [%s]",
                request.getPaymentOperationType(), getPaymentProcessorName()));
        Response instance = null;
        try {
            instance = (Response) clazz.newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            log.error("Fail to generate error response due to [{}]", e);
            return null;
        }
        instance.setResponseCode(ResponseStatus.INTERNAL_ERROR_CODE);
        instance.setResponseDescription(String.format(
                "Invalid request! Payment operation type [%s] is not supported", request.getPaymentOperationType()));
        record(null, "Unvalid request error", ErrorCode.OPERATION_UNSUPPORT.code());
        return instance;
    }

    /**
     * Get a reference from the input map.
     * @param references References.
     * @return reference ID.
     */
    protected String getReference(final Map<String, String> references) {
        // Currently we only support single reference cases.
        return references.keySet().iterator().next();
    }

    /**
     * Invoke charge operation on the request.
     * @param request Create payment request request.
     * @return CreatePaymentResponse.
     */
    public abstract CreatePaymentResponse invokeChargeOperation(final CreatePaymentRequest request);

    /**
     * Add an error action in DB.
     * @param transactionID Transaction id.
     * @param message Error detail.
     * @param code Error code.
     */
    protected void record(final String transactionID, final String message, final int code) {
        log.error(message);
        ActionRecord record = ActionRecordBuilder.builder()
                .time(Timestamp.valueOf(LocalDateTime.now()))
                .errorCode(code)
                .transactionID(transactionID)
                .message(message)
                .build();
        actionRecordDAO.record(record);
    }

    /**
     * Hook for sub-class.
     * @param request Incoming CreateOrUpdatePaymentResponseRequest instance.
     * @param requests Payment requests.
     * @param responses Payment responses.
     */
    protected void updateList(final CreateOrUpdatePaymentResponseRequest request,
            final List<PaymentRequest> requests, final List<PaymentResponse> responses) {
    }

    /**
     * Invoke refund operation on the request.
     * @param request Refund request.
     * @return RefundResponse.
     */
    public abstract RefundResponse invokeRefundOperation(final RefundRequest request);

    /**
     * Invoke sign operation on the request.
     * @param request Sign request.
     * @return CreatePaymentResponse.
     */
    protected CreatePaymentResponse invokeSignOperation(final CreatePaymentRequest request) {
        return (CreatePaymentResponse) logAndReturn(request, REQUEST_TO_RESPONSE_MAPPING.get(request.getClass()));
    }

    /**
     * Invoke ScheduledPay operation on the request.
     * @param request ScheduledPay request.
     * @return ScheduledPayResponse.
     */
    protected ScheduledPayResponse invokeScheduledPayOperation(final ScheduledPayRequest request) {
        return (ScheduledPayResponse) logAndReturn(request, REQUEST_TO_RESPONSE_MAPPING.get(request.getClass()));
    }

    /**
     * Invoke rescind operation on the request.
     * @param request Rescind request.
     * @return RescindResponse.
     */
    protected RescindResponse invokeRescindOperation(final RescindRequest request) {
        return (RescindResponse) logAndReturn(request, REQUEST_TO_RESPONSE_MAPPING.get(request.getClass()));
    }

    /**
     * Get the corresponding payment request's transaction id from the incoming response.
     * @param request CreateOrUpdatePaymentResponseRequest request.
     * @return Transaction ID.
     */
    public abstract String getTransactionID(final CreateOrUpdatePaymentResponseRequest request);

    /**
     * Get the 3P payment gateway's transaction id from the incoming response.
     * @param request CreateOrUpdatePaymentResponseRequest request.
     * @return ExternalTransactionID.
     */
    public abstract String getExternalTransactionID(final CreateOrUpdatePaymentResponseRequest request);

    /**
     * Get the payment response's status.
     * @param request CreateOrUpdatePaymentResponseRequest request.
     * @return Response status.
     */
    public abstract int getStatus(final CreateOrUpdatePaymentResponseRequest request);

    /**
     * Return success code.
     * @return Success code.
     */
    protected String success() {
        return "success";
    }

    /**
     * Return fail code.
     * @return Failure code.
     */
    protected String fail() {
        return "error";
    }

    private boolean deducePaymentAccount(final Request request, final String paymentMethod) {
        Pair<String, String> pair = accountDeducer.deduce(Pair.of(request, paymentMethod));
        String accountName = pair.getLeft();
        String material = accountFactory.getMaterialNameByAccountName(accountName);
        if (material == null) {
            return false;
        }

        Account deducedAccount = accountFactory.getAccount(accountName);
        if (deducedAccount.getAccountNo() == null) {
            deducedAccount.setAccountNo(pair.getRight());
        }

        getAccount().set(deducedAccount);
        AccountUtils.set(deducedAccount);
        return true;
    }

    private Response generatePaymentAccountNotFoundResponse() {
        Response response = new Response();
        response.setResponseCode(500);
        response.setResponseDescription("Could not load proper payment account.");
        return response;
    }

    private void generateTransactionID(final BusinessProfile businessProfile, final String operationType) {
        StringBuilder sb = new StringBuilder(64);
        String signature = getSignatureGenerator().generate();
        sb.append(getSignatureGenerator().generate().substring(0, signature.length() - 4))
            .append(businessProfile.code())
            .append(PAYMENT_OPERATION_CODES.get(operationType));
        transactionID.set(sb.toString());
    }

    private CreatePaymentResponse raiseErrorOnDuplicatedRequest(final CreatePaymentRequest createPaymentRequest) {
        CreatePaymentResponse response = new CreatePaymentResponse();
        FastSearchTableItem item = getFastSearchTableDAO().find("sign"
                + createPaymentRequest.getCustomerID() + createPaymentRequest.getBusiness());
        if (item != null) {
            log.warn("Duplicated sign request for the same customer [{}] and business [{}], something wrong happened!",
                    createPaymentRequest.getCustomerID(), createPaymentRequest.getBusiness());
            response.setResponseCode(ResponseStatus.INTERNAL_ERROR_CODE);
            response.setResponseDescription("Duplicated request, the customer has signed contract with cloud music.");
            return response;
        }
        return null;
    }

    private Response execute(final PaymentOperation paymentOperation, final Request request) {
        return paymentOperation.execute(new Visitor<Request, Response>() {

            @Override
            public Response onCharge(final Request request) {
                CreatePaymentRequest createPaymentRequest = (CreatePaymentRequest) request;
                return invokeChargeOperation(createPaymentRequest);
            }

            @Override
            public Response onSign(final Request request) {
                CreatePaymentRequest createPaymentRequest = (CreatePaymentRequest) request;
                CreatePaymentResponse response = raiseErrorOnDuplicatedRequest(createPaymentRequest);
                if (response != null) {
                    return response;
                }
                return invokeSignOperation(createPaymentRequest);
            }

            @Override
            public Response onSignAndCharge(final Request request) {
                return logAndReturn(request, REQUEST_TO_RESPONSE_MAPPING.get(request.getClass()));
            }

            @Override
            public Response onRefund(final Request request) {
                RefundRequest refundRequest = (RefundRequest) request;
                return invokeRefundOperation(refundRequest);
            }

            @Override
            public Response onRescind(final Request request) {
                RescindRequest rescindRequest = (RescindRequest) request;
                return invokeRescindOperation(rescindRequest);
            }

            @Override
            public Response onScheduledPay(final Request request) {
                ScheduledPayRequest scheduledPayRequest = (ScheduledPayRequest) request;
                return invokeScheduledPayOperation(scheduledPayRequest);
            }

            @Override
            public Response onTransfer(final Request request) {
                // TODO Auto-generated method stub
                return null;
            }

        }, request);
    }

    /**
     * Handle the input 3P payment gateway response.
     * @param request CreateOrUpdatePaymentResponseRequest request.
     * @return CreateOrUpdatePaymentResponseResponse response.
     */
    private CreateOrUpdatePaymentResponseResponse handleResponse(final CreateOrUpdatePaymentResponseRequest request) {

        log.info("Begin to process notification for [{}] with operation [{}]", request.getPaymentMethodName(), request.getPaymentOperationType());

        CreateOrUpdatePaymentResponseResponse response = new CreateOrUpdatePaymentResponseResponse();
        response.setResponseCode(ResponseStatus.INTERNAL_ERROR_CODE);
        response.setResponseDescription(fail());

        PaymentResponse paymentResponse = null;
        PaymentRequest paymentRequest = null;
        Timestamp now = Timestamp.valueOf(LocalDateTime.now());

        // Validate incoming request.
        if (!getValidator().validate(request)) {
            record(null, String.format("Fail to validate response for payment method [%s] with operation [%s], request detail [%s]",
                            request.getPaymentMethodName(), request.getPaymentOperationType(), Jackson.json(request)),
                    ErrorCode.VALIDATION_FAIL.code());
            return response;
        }

        String paymentTransactionID = getTransactionID(request);
        paymentRequest = getPaymentRequestDAO().getPaymentRequestByTransactionID(paymentTransactionID);

        if (paymentRequest == null) {
            record(paymentTransactionID, String.format("Receive payment response for unexisted request [%s]", paymentTransactionID),
                    ErrorCode.DUPLICATE_RESPONSE.code());
            return response;
        }

        PaymentResponse existedResponse = getPaymentResponseDAO().getPaymentResponseByTransactionID(paymentTransactionID);
        Pair<Boolean, PaymentResponse> duplicatedResponseCheckResult = checkAndPrepareResponse(existedResponse, paymentRequest, request, now);
        if (duplicatedResponseCheckResult.getLeft()) {
            record(paymentTransactionID, String.format("Duplicate payment reponse [%s], response has been updated successfully", paymentTransactionID),
                    ErrorCode.DUPLICATE_RESPONSE.code());
            return response;
        }
        paymentResponse = duplicatedResponseCheckResult.getRight();

        Pair<Boolean, String> callbackResult = invokeCallback(paymentRequest, request);
        if (!callbackResult.getLeft()) {
            record(paymentTransactionID,
                    String.format("Fail to call URL : [%s] due to [%s]", paymentRequest.getCallBackMetaInfo(), callbackResult.getRight()),
                    ErrorCode.CALLBACK_FAIL.code());
            return response;
        }

        update(paymentResponse, paymentRequest, now, request);
        response.setResponseCode(ResponseStatus.PROCESS_COMPLETE_CODE);
        response.setResponseDescription(success());

        log.info("Notification processed without any exception");
        return response;
    }

    private Pair<Boolean, PaymentResponse> checkAndPrepareResponse(final PaymentResponse existedResponse, final PaymentRequest paymentRequest,
                final CreateOrUpdatePaymentResponseRequest request, final Timestamp now) {
        Pair<Boolean, PaymentResponse> pair = Pair.of(false, null);
        PaymentResponse paymentResponse;

        if (existedResponse != null) {
            if (existedResponse.getStatus() == PaymentResponseStatus.SUCCESS.status()) {
                pair = Pair.of(true, null);
                return pair;
            }
            paymentResponse = existedResponse;
            paymentResponse.setLastUpdateTime(now);
            paymentResponse.setStatus(getStatus(request));
        } else {
            paymentResponse = PaymentResponseBuilder.builder()
                    .createTime(now)
                    .externalTransactionID(getExternalTransactionID(request))
                    .lastUpdateTime(now)
                    .operationType(request.getPaymentOperationType())
                    .paymentMethod(getPaymentProcessorName())
                    .rawResponse(Jackson.json(request))
                    .referenceID(paymentRequest.getReferenceID())
                    .transactionID(paymentRequest.getTransactionID())
                    .status(getStatus(request))
                    .business(paymentRequest.getBusiness())
                    .customerID(paymentRequest.getCustomerID())
                    .acknowledgedAmount(paymentRequest.getRequestedAmount())
                    .build();
        }
        pair = Pair.of(false, paymentResponse);
        return pair;
    }

    private Pair<Boolean, String> invokeCallback(final PaymentRequest paymentRequest, final CreateOrUpdatePaymentResponseRequest request) {
        Pair<Boolean, String> pair = Pair.of(true, null);
        CallbackMetaInfo callbackMetaInfo = null;
        if (paymentRequest.getCallBackMetaInfo() != null && !paymentRequest.getCallBackMetaInfo().equals("null")
                && !paymentRequest.getCallBackMetaInfo().equals(StringUtils.EMPTY)) {
            callbackMetaInfo = Jackson.parse(paymentRequest.getCallBackMetaInfo(), CallbackMetaInfo.class);
            if (callbackMetaInfo.getParameters() == null) {
                callbackMetaInfo.setParameters(new HashMap<>());
            }
            callbackMetaInfo.getParameters().put("status", String.valueOf(getStatus(request)));
            callbackMetaInfo.getParameters().put("transactionID", paymentRequest.getTransactionID());
            callbackMetaInfo.getParameters().put("paymentMethod", getPaymentProcessorName());
            CallbackDetail callbackDetail = getCallback().call(callbackMetaInfo);
            pair = Pair.of(callbackDetail.isSucceed(), callbackDetail.getError());
        }
        return pair;
    }

    private void update(final PaymentResponse paymentResponse, final PaymentRequest paymentRequest, final Timestamp now,
            final CreateOrUpdatePaymentResponseRequest request) {
        paymentRequest.setStatus(PaymentRequestStatus.PAID.status());
        paymentRequest.setLastUpdateTime(now);
        List<PaymentResponse> responses = new LinkedList<>();
        List<PaymentRequest> requests = new LinkedList<>();
        requests.add(paymentRequest);
        responses.add(paymentResponse);
        updateList(request, requests, responses);
        getPaymentsDAOService().updateRequestAndResponse(requests, responses);
    }
}
