package org.wgx.payments.processors;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.SortedMap;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.wgx.payments.builder.PaymentRequestBuilder;
import org.wgx.payments.client.api.helper.BusinessProfile;
import org.wgx.payments.client.api.helper.PaymentChannel;
import org.wgx.payments.client.api.helper.PaymentMethod;
import org.wgx.payments.client.api.helper.PaymentOperation;
import org.wgx.payments.client.api.helper.PaymentResponseStatus;
import org.wgx.payments.client.api.helper.ResponseStatus;
import org.wgx.payments.client.api.io.CreateOrUpdatePaymentResponseRequest;
import org.wgx.payments.client.api.io.CreatePaymentRequest;
import org.wgx.payments.client.api.io.CreatePaymentResponse;
import org.wgx.payments.client.api.io.RefundRequest;
import org.wgx.payments.client.api.io.RefundResponse;
import org.wgx.payments.client.api.io.RescindRequest;
import org.wgx.payments.client.api.io.RescindResponse;
import org.wgx.payments.client.api.io.ScheduledPayRequest;
import org.wgx.payments.client.api.io.ScheduledPayResponse;
import org.wgx.payments.execution.SimplePaymentProcessor;
import org.wgx.payments.model.ActionRecord.ErrorCode;
import org.wgx.payments.model.PaymentRequest;
import org.wgx.payments.model.PaymentRequestStatus;
import org.wgx.payments.model.PaymentResponse;
import org.wgx.payments.tools.Jackson;
import org.wgx.payments.utils.AlipayConstants;
import org.wgx.payments.utils.AlipayUtils;

import lombok.extern.slf4j.Slf4j;

/**
 * Alipay payment processor.
 *
 */
@Slf4j
public class AlipayProcessor extends SimplePaymentProcessor {

    private static final String NAME = PaymentMethod.ALIPAY.paymentMethodName();
    private static final String OUT_TRADE_NO = "out_trade_no";
    private static final String BATCH_NO = "batch_no";
    private static final String TRADE_STATUS = "trade_status";
    private static final String STATUS = "status";
    private static final String EXTERNAL_SIGN_NO = "external_sign_no";
    private static final String TRADE_NO = "trade_no";
    private static final String AGREEMENT_NO = "agreement_no";
    private static final String ALIPAY_USER_ID = "alipay_user_id";
    private static final String ERROR = "error";

    /**
     * {@inheritDoc}
     */
    @Override
    public String getPaymentProcessorName() {
        return NAME;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CreatePaymentResponse invokeChargeOperation(final CreatePaymentRequest request) {
        CreatePaymentResponse response = new CreatePaymentResponse();
        String transactionID = getTransactionID().get();
        SortedMap<String, String> sortedMap = AlipayUtils.buildParameterMap(request);
        sortedMap.put(OUT_TRADE_NO, transactionID);
        BusinessProfile profile = BusinessProfile.fromProfile(request.getBusiness());
        sortedMap.put("subject", profile.displayName());
        boolean isMobile = PaymentChannel.MOBILE.channel().equals(request.getChannel());

        log.info("Alipay charge request from client channel [{}]", request.getChannel());
        String queryString = AlipayUtils.buildRequestParaStr(sortedMap, isMobile,
                getAccount().get().getPrivateKey());
        String url = queryString;
        if (!isMobile) {
            url = AlipayConstants.GATEWAY_URL + queryString;
        }

        log.info("Generate payment url [{}] for the incoming request", url);
        Timestamp now = Timestamp.valueOf(LocalDateTime.now());
        PaymentRequest paymentRequest = PaymentRequestBuilder.builder()
                .channel(request.getChannel())
                .createTime(now)
                .customerID(request.getCustomerID())
                .lastUpdateTime(now)
                .parentID(StringUtils.EMPTY)
                .paymentMethod(NAME)
                .paymentOperationType(PaymentOperation.CHARGE.operationType())
                .referenceID(getReference(request.getReferences()))
                .requestedAmount(request.getReferences().get(getReference(request.getReferences())))
                .status(PaymentRequestStatus.PENDING.status())
                .transactionID(transactionID)
                .url(url)
                .callBackMetaInfo(Jackson.json(request.getCallbackInfo()))
                .business(request.getBusiness())
                .build();
        getPaymentRequestDAO().save(paymentRequest);
        response.setResponseCode(ResponseStatus.PROCESS_SUCCESS_CODE);
        response.setTransactionID(transactionID);
        response.setUrl(url);
        return response;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public RefundResponse invokeRefundOperation(final RefundRequest request) {
        RefundResponse response = new RefundResponse();
        response.setResponseCode(ResponseStatus.PROCESS_SUCCESS_CODE);
        PaymentResponse chargeResponse = getLocalResponse().get();
        SortedMap<String, String> parameters = AlipayUtils.generateRefundParameters(chargeResponse.getExternalTransactionID(),
                getTransactionID().get(), request.getRefundAmount());
        String url = AlipayConstants.GATEWAY_URL + AlipayUtils.buildRequestParaStr(parameters, false,
                getAccount().get().getPrivateKey());

        /**
         *  This special channel is mainly used to differentiate the auto refund request triggered by Payments-Platform
         *  and the normal refund request from clients.
         */
        String specialChannel = null;
        if (request.getSpecialTags() != null) {
            specialChannel = request.getSpecialTags().get("channel");
        }
        Timestamp now = Timestamp.valueOf(LocalDateTime.now());
        PaymentRequest refundPaymentRequest = PaymentRequestBuilder.builder()
                .business(request.getBusiness())
                .callBackMetaInfo(Jackson.json(request.getCallbackInfo()))
                .channel(specialChannel)
                .createTime(now)
                .customerID(request.getCustomerID())
                .lastUpdateTime(now)
                .parentID(chargeResponse.getTransactionID())
                .paymentMethod(getPaymentProcessorName())
                .paymentOperationType(PaymentOperation.REFUND.operationType())
                .referenceID(chargeResponse.getReferenceID())
                .requestedAmount(request.getRefundAmount())
                .status(PaymentRequestStatus.UNVALID.status())
                .transactionID(getTransactionID().get())
                .url(url)
                .build();
        getPaymentRequestDAO().save(refundPaymentRequest);

        Triple<Boolean, Element, Element> alipayResult = callAndParse(url);
        if (!alipayResult.getLeft()) {
            log.warn("Fail to send refund request to Alipay");
            response.setResponseCode(ResponseStatus.INTERNAL_ERROR_CODE);
            response.setResponseDescription("Fail to send refund request to Alipay");
            return response;
        }

        if ("T".equals(alipayResult.getRight().getText())) {
            refundPaymentRequest.setStatus(PaymentRequestStatus.PENDING.status());
        } else if ("F".equals(alipayResult.getRight().getText())) {
            refundPaymentRequest.setStatus(PaymentRequestStatus.UNVALID.status());
            response.setResponseCode(ResponseStatus.INTERNAL_ERROR_CODE);
            response.setResponseDescription(alipayResult.getMiddle().element(ERROR).getText());
            record(getTransactionID().get(),
                    String.format("Alipay refund failed due to [%s]", alipayResult.getMiddle().element(ERROR).getText()),
                        ErrorCode.DUPLICATE_RESPONSE.code());
        } else {
            response.setResponseCode(ResponseStatus.INTERNAL_ERROR_CODE);
            response.setResponseDescription(alipayResult.getMiddle().element(ERROR).getText());
            refundPaymentRequest.setStatus(PaymentRequestStatus.PENDING_ON_RETRY.status());
        }
        response.setTransactionID(getTransactionID().get());
        getPaymentRequestDAO().save(refundPaymentRequest);

        return response;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CreatePaymentResponse invokeSignOperation(final CreatePaymentRequest request) {
        CreatePaymentResponse response = new CreatePaymentResponse();
        PaymentRequest signRequest;
        Timestamp now = Timestamp.valueOf(LocalDateTime.now());
        signRequest = PaymentRequestBuilder.builder()
                .createTime(now)
                .business(request.getBusiness())
                .callBackMetaInfo(Jackson.json(request.getCallbackInfo()))
                .channel(request.getChannel())
                .customerID(request.getCustomerID())
                .lastUpdateTime(now)
                .parentID(StringUtils.EMPTY)
                .paymentMethod(getPaymentProcessorName())
                .paymentOperationType(PaymentOperation.SIGN.operationType())
                .referenceID(getReference(request.getReferences()))
                .requestedAmount(StringUtils.EMPTY)
                .transactionID(getTransactionID().get())
                .status(PaymentRequestStatus.PENDING.status())
                .build();
        SortedMap<String, String> parameters = AlipayUtils.generateSignParameters(getTransactionID().get(),
                null, request.getChannel());
        String url = AlipayConstants.GATEWAY_URL + AlipayUtils.buildRequestParaStr(parameters, false,
                getAccount().get().getPrivateKey());
        signRequest.setUrl(url);

        log.info(String.format("Generate Alipay sign url [%s] for customer [%s] with business [%s]", url, request.getCustomerID(), request.getBusiness()));

        getPaymentRequestDAO().save(signRequest);
        response.setResponseCode(ResponseStatus.PROCESS_SUCCESS_CODE);
        response.setTransactionID(signRequest.getTransactionID());
        response.setUrl(url);
        return response;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ScheduledPayResponse invokeScheduledPayOperation(final ScheduledPayRequest request) {
        ScheduledPayResponse response = new ScheduledPayResponse();
        response.setResponseCode(ResponseStatus.PROCESS_SUCCESS_CODE);
        String transactionID = getTransactionID().get();
        SortedMap<String, String> contents = AlipayUtils.generateScheduledPayParameter(transactionID, request.getAmount(),
                StringUtils.split(getLocalResponse().get().getExternalTransactionID(), "^")[0]);
        BusinessProfile profile = BusinessProfile.fromProfile(request.getBusiness());
        contents.put("subject", profile.displayName());
        String url = AlipayConstants.GATEWAY_URL + AlipayUtils.buildRequestParaStr(contents, false,
                getAccount().get().getPrivateKey());

        Timestamp now = Timestamp.valueOf(LocalDateTime.now());
        PaymentRequest scheduledPayRequest = PaymentRequestBuilder.builder()
                .business(request.getBusiness())
                .callBackMetaInfo(Jackson.json(request.getCallbackInfo()))
                .channel(StringUtils.EMPTY)
                .createTime(now)
                .customerID(request.getCustomerID())
                .lastUpdateTime(now)
                .parentID(getLocalResponse().get().getTransactionID())
                .paymentMethod(getPaymentProcessorName())
                .paymentOperationType(PaymentOperation.SCHEDULEDPAY.operationType())
                .referenceID(getLocalResponse().get().getReferenceID())
                .requestedAmount(request.getAmount())
                .status(PaymentRequestStatus.UNVALID.status())
                .transactionID(transactionID)
                .url(url)
                .build();
        getPaymentRequestDAO().save(scheduledPayRequest);

        Triple<Boolean, Element, Element> alipayResult = callAndParse(url);
        if (!alipayResult.getLeft()) {
            log.warn("Fail to send scheduled request to Alipay");
            response.setResponseCode(ResponseStatus.INTERNAL_ERROR_CODE);
            response.setResponseDescription("Fail to send scheduled request to Alipay");
            return response;
        }

        if ("T".equals(alipayResult.getRight().getText())) {
            String resultCode = alipayResult.getMiddle().element("response").element("alipay").element("result_code").getText();
            if ("ORDER_SUCCESS_PAY_FAIL".equals(resultCode) || "ORDER_FAIL".equals(resultCode)) {
                response.setResponseCode(ResponseStatus.INTERNAL_ERROR_CODE);
                String resultDetail = alipayResult.getMiddle().element("response").element("alipay").element("detail_error_code").getText();
                response.setResponseDescription(resultDetail);
                record(transactionID, "Alipay scheduled pay failed due to " + resultDetail, ErrorCode.PAYMENT_FAIL.code());
            } else {
                scheduledPayRequest.setStatus(PaymentRequestStatus.PENDING.status());
            }
        } else if ("F".equals(alipayResult.getRight().getText())) {
            response.setResponseCode(ResponseStatus.INTERNAL_ERROR_CODE);
            response.setResponseDescription(alipayResult.getMiddle().element(ERROR).getText());
            record(transactionID, String.format("Alipay scheduled pay failed due to %s", alipayResult.getMiddle().element(ERROR).getText()),
                    ErrorCode.PAYMENT_FAIL.code());
        }
        response.setTransactionID(transactionID);
        getPaymentRequestDAO().save(scheduledPayRequest);

        return response;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public RescindResponse invokeRescindOperation(final RescindRequest request) {
        RescindResponse rescindResponse = new RescindResponse();
        PaymentRequest rescindRequest;
        Timestamp now = Timestamp.valueOf(LocalDateTime.now());
        String rescindTransactionID = getTransactionID().get();
        String alipayUserID = StringUtils.split(getLocalResponse().get().getExternalTransactionID(), "^")[1];
        SortedMap<String, String> parameters = AlipayUtils.generateRescindParameters(getLocalResponse().get().getTransactionID(), alipayUserID);
        String queryString = AlipayUtils.buildRequestParaStr(parameters, false,
                getAccount().get().getPrivateKey());
        String url = AlipayConstants.GATEWAY_URL + queryString;

        String specialChannel = null;
        if (request.getSpecialTags() != null) {
            specialChannel = request.getSpecialTags().get("channel");
        }
        rescindRequest = PaymentRequestBuilder.builder()
                .business(request.getBusiness())
                .callBackMetaInfo(Jackson.json(request.getCallbackInfo()))
                .channel(specialChannel)
                .createTime(now)
                .customerID(request.getCustomerID())
                .lastUpdateTime(now)
                .parentID(getLocalResponse().get().getTransactionID())
                .paymentMethod(getPaymentProcessorName())
                .paymentOperationType(PaymentOperation.RESCIND.operationType())
                .referenceID(getLocalResponse().get().getReferenceID())
                .requestedAmount("0")
                .status(PaymentRequestStatus.UNVALID.status())
                .transactionID(rescindTransactionID)
                .url(url)
                .build();
        getPaymentRequestDAO().save(rescindRequest);

        Triple<Boolean, Element, Element> alipayResult = callAndParse(url);
        if (!alipayResult.getLeft()) {
            rescindResponse.setResponseCode(ResponseStatus.INTERNAL_ERROR_CODE);
            rescindResponse.setResponseDescription("Fail to send Rescind request to Alipay");
            return rescindResponse;
        }

        if ("T".equals(alipayResult.getRight().getText())) {
            rescindRequest.setStatus(PaymentRequestStatus.PENDING.status());
            rescindResponse.setResponseCode(ResponseStatus.PROCESS_SUCCESS_CODE);
        } else {
            rescindRequest.setStatus(PaymentRequestStatus.UNVALID.status());
            rescindResponse.setResponseCode(ResponseStatus.INTERNAL_ERROR_CODE);
            rescindResponse.setResponseDescription(alipayResult.getMiddle().element(ERROR).getText());
            // Special case, the customer has deleted his account in Alipay.
            if (rescindResponse.getResponseDescription().equals("USER_NOT_EXSIT_ERROR")) {
                String key = "sign" + rescindRequest.getCustomerID() + rescindRequest.getBusiness();
                getFastSearchTableDAO().deleteItemByKey(key);
                rescindRequest.setStatus(PaymentRequestStatus.UNVALID.status());
            }
        }
        rescindResponse.setTransactionID(rescindTransactionID);
        getPaymentRequestDAO().save(rescindRequest);

        return rescindResponse;
    }

    /**
     * {@inheritDoc}
     */
    public String getTransactionID(final CreateOrUpdatePaymentResponseRequest request) {
        if (PaymentOperation.CHARGE.operationType().equals(request.getPaymentOperationType())) {
            return request.getParameters().get(OUT_TRADE_NO)[0];
        }
        if (PaymentOperation.SCHEDULEDPAY.operationType().equals(request.getPaymentOperationType())) {
            return request.getParameters().get(OUT_TRADE_NO)[0];
        }
        if (PaymentOperation.SIGN.operationType().equals(request.getPaymentOperationType())) {
            return request.getParameters().get(EXTERNAL_SIGN_NO)[0];
        }
        if (PaymentOperation.REFUND.operationType().equals(request.getPaymentOperationType())) {
            String batchNo = request.getParameters().get(BATCH_NO)[0];
            int length = batchNo.length();
            return batchNo.substring(8, length);
        }
        if (PaymentOperation.RESCIND.operationType().equals(request.getPaymentOperationType())) {
            /**
             * Rescind response does not contain the payment service's rescind request's transaction id,
             * we'll have to query the db to get that.
             */
            String signRequestTransactionID = request.getParameters().get(EXTERNAL_SIGN_NO)[0];
            List<PaymentRequest> requests = getPaymentRequestDAO().getPaymentRequestsByParentID(signRequestTransactionID);
            PaymentRequest rescindRequest = requests.stream().filter(subRequest -> {
                    return subRequest.getPaymentOperationType().equals(PaymentOperation.RESCIND.operationType());
                })
                .findAny()
                .get();
            return rescindRequest.getTransactionID();
        }
        return null;
    }

    /**
     * {@inheritDoc}
     */
    public String getExternalTransactionID(final CreateOrUpdatePaymentResponseRequest request) {
        if (PaymentOperation.CHARGE.operationType().equals(request.getPaymentOperationType())) {
            return request.getParameters().get(TRADE_NO)[0];
        }
        if (PaymentOperation.SIGN.operationType().equals(request.getPaymentOperationType())) {
            return request.getParameters().get(AGREEMENT_NO)[0]
                    + "^" + request.getParameters().get(ALIPAY_USER_ID)[0];
        }
        if (PaymentOperation.REFUND.operationType().equals(request.getPaymentOperationType())) {
            /**
             * For refund responses, Alipay does not provide external transaction id.
             * We can use the batch_no(the refund request's transaction id to query the refund status.)
             */
            return request.getParameters().get(BATCH_NO)[0];
        }
        if (PaymentOperation.SCHEDULEDPAY.operationType().equals(request.getPaymentOperationType())) {
            return request.getParameters().get(TRADE_NO)[0];
        }
        if (PaymentOperation.RESCIND.operationType().equals(request.getPaymentOperationType())) {
            return request.getParameters().get(AGREEMENT_NO)[0]
                    + "^" + request.getParameters().get(ALIPAY_USER_ID)[0];
        }
        return null;
    }

    /**
     * {@inheritDoc}
     */
    public int getStatus(final CreateOrUpdatePaymentResponseRequest request) {
        if (PaymentOperation.CHARGE.operationType().equals(request.getPaymentOperationType())) {
            String status = request.getParameters().get(TRADE_STATUS)[0];
            if ("TRADE_SUCCESS".equalsIgnoreCase(status) || "TRADE_FINISHED".equalsIgnoreCase(status)) {
                return PaymentResponseStatus.SUCCESS.status();
            }
        }
        if (PaymentOperation.REFUND.operationType().equals(request.getPaymentOperationType())) {
            String resultDetails = request.getParameters().get("result_details")[0];
            String[] result = StringUtils.split(resultDetails, "^");
            if ("SUCCESS".equalsIgnoreCase(result[2])) {
                return PaymentResponseStatus.SUCCESS.status();
            }
        }
        if (PaymentOperation.SIGN.operationType().equals(request.getPaymentOperationType())) {
            String status = request.getParameters().get(STATUS)[0];
            if ("NORMAL".equalsIgnoreCase(status)) {
                return PaymentResponseStatus.SUCCESS.status();
            }
        }
        if (PaymentOperation.RESCIND.operationType().equals(request.getPaymentOperationType())) {
            String status = request.getParameters().get(STATUS)[0];
            if ("UNSIGN".equalsIgnoreCase(status)) {
                return PaymentResponseStatus.SUCCESS.status();
            }
        }
        if (PaymentOperation.SCHEDULEDPAY.operationType().equals(request.getPaymentOperationType())) {
            String status = request.getParameters().get(TRADE_STATUS)[0];
            if ("TRADE_SUCCESS".equalsIgnoreCase(status) || "TRADE_FINISHED".equalsIgnoreCase(status)) {
                return PaymentResponseStatus.SUCCESS.status();
            }
        }
        return PaymentResponseStatus.FAIL.status();
    }

    /**
     * Call Alipay server and parse the response.
     * @param url Alipay server's HTTP URL.
     * @return Parsed Alipay response,
     *      left : flag indicates if we called Alipay successfully.
     *      middle : Parsed xml document's root element.
     *      right : Parse xml document's is_success element.
     */
    private Triple<Boolean, Element, Element> callAndParse(final String url) {
        Triple<Boolean, Element, Element> alipayResult;
        Pair<String, String> pair = Pair.of(url, null);
        String result = getFacade().call(pair);
        try {
            Document document = DocumentHelper.parseText(result);
            Element root = document.getRootElement();
            Element statusElement = root.element("is_success");
            alipayResult = Triple.of(true, root, statusElement);
        } catch (Exception e) {
            alipayResult = Triple.of(false, null, null);
            log.error("Fail to connect to alipay.", e);
        }
        return alipayResult;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void updateList(final CreateOrUpdatePaymentResponseRequest request,
            final List<PaymentRequest> requests, final List<PaymentResponse> responses) {
        if (request.getPaymentOperationType().equals(PaymentOperation.RESCIND.operationType())) {
            //解约结果返回时不仅需要更新本次解约请求的状态，还需要更新之前签约的结果，将其标示为已解除签约。
            PaymentResponse signResponse = getPaymentResponseDAO().getPaymentResponseByTransactionID(requests.get(0).getParentID());
            signResponse.setStatus(PaymentResponseStatus.UNVALID.status());
            responses.add(signResponse);
        }
    }

}
