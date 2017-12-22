package org.wgx.payments.processors;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;
import org.wgx.payments.builder.PaymentRequestBuilder;
import org.wgx.payments.builder.PaymentResponseBuilder;
import org.wgx.payments.client.api.helper.BusinessProfile;
import org.wgx.payments.client.api.helper.PaymentMethod;
import org.wgx.payments.client.api.helper.PaymentOperation;
import org.wgx.payments.client.api.helper.PaymentResponseStatus;
import org.wgx.payments.client.api.helper.ResponseStatus;
import org.wgx.payments.client.api.io.CreateOrUpdatePaymentResponseRequest;
import org.wgx.payments.client.api.io.CreatePaymentRequest;
import org.wgx.payments.client.api.io.CreatePaymentResponse;
import org.wgx.payments.client.api.io.RefundRequest;
import org.wgx.payments.client.api.io.RefundResponse;
import org.wgx.payments.client.api.io.Request;
import org.wgx.payments.client.api.io.RescindRequest;
import org.wgx.payments.client.api.io.RescindResponse;
import org.wgx.payments.client.api.io.ScheduledPayRequest;
import org.wgx.payments.client.api.io.ScheduledPayResponse;
import org.wgx.payments.deducer.Deducer;
import org.wgx.payments.deducer.WechatURLDeducer;
import org.wgx.payments.execution.SimplePaymentProcessor;
import org.wgx.payments.model.ActionRecord.ErrorCode;
import org.wgx.payments.model.PaymentRequest;
import org.wgx.payments.model.PaymentRequestStatus;
import org.wgx.payments.model.PaymentResponse;
import org.wgx.payments.tools.Jackson;
import org.wgx.payments.utils.WechatConstants;
import org.wgx.payments.utils.WechatSignatureHelper;
import org.wgx.payments.utils.WechatUtils;
import org.wgx.payments.utils.XMLUtils;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

/**
 * Wechat payment processor.
 *
 */
@Slf4j
public class WechatProcessor extends SimplePaymentProcessor {

    private static final String NAME = PaymentMethod.WECHAT.paymentMethodName();
    private static final String SUCCESS = "SUCCESS";
    private static final String USER_IP = "user_ip";
    private static final String OPENID = "openid";

    @Setter
    private Deducer<CreatePaymentRequest, String> tradeTypeDeducer;
    @Setter
    private Deducer<Triple<CreatePaymentRequest, String, Map<String, Object>>, String> urlDeducer = new WechatURLDeducer();

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
        // Prepare charge request needed data.
        CreatePaymentResponse response = new CreatePaymentResponse();
        response.setResponseCode(ResponseStatus.INTERNAL_ERROR_CODE);
        String transactionID = getTransactionID().get();
        String referenceID = getReference(request.getReferences());
        String tradeType = tradeTypeDeducer.deduce(request);
        String appID = getAccount().get().getPrivateKey();

        // Updated at 2017/07/28 for Wechat H5 pay scenarios.
        if (tradeType.equals("MWEB") && (request.getSpecialTags() == null || request.getSpecialTags().get(USER_IP) == null)) {
            response.setResponseDescription("User ip must specified when calling mweb API");
            return response;
        }

        processAdditionalParameters(request);

        // Generate charge request's URL based on Wechat's demand.
        log.info("Use client [{}] to communicate Wechat to generate charge url", tradeType);
        BusinessProfile profile = BusinessProfile.fromProfile(request.getBusiness());
        Map<String, Object> parameters = WechatUtils.buildChargeParameters(transactionID, request.getReferences().get(referenceID),
                tradeType, appID, profile.displayName(), getAccount().get().getPublicKey());

        // Call Wechat and parse the result.
        Pair<Boolean, Map<String, Object>> wechatResult = callAndParse(WechatConstants.CHARGE_API, XMLUtils.mapToXmlStr(parameters));
        if (!wechatResult.getLeft()) {
            log.error("Fail to parse Wechat returned response");
            response.setResponseDescription("Fail to parse Wechat returned response");
            return response;
        }

        // Verify and return the URL to the upstream clients if possible.
        Map<String, Object> responseMap = wechatResult.getRight();
        boolean verify = WechatSignatureHelper.signVerifyWithKey(responseMap,
                getAccount().get().getPublicKey());
        if (verify) {
            parseChargeResponse(responseMap, tradeType, request, referenceID, response, getAccount().get().getAccountName());
        } else {
            log.warn("Fail to verify wechat charge response.");
            response.setResponseDescription("Wechat response verify error");
        }

        return response;
    }

    private void processAdditionalParameters(final CreatePaymentRequest request) {
        // Process additional parameters.
        if (request.getSpecialTags() != null) {
            Map<String, String> map = new HashMap<>();
            // Normal use case.
            if (request.getSpecialTags().get(USER_IP) != null) {
                map.put(USER_IP, request.getSpecialTags().get(USER_IP));
            }
            // Public account pay use case.
            if (request.getSpecialTags().get(OPENID) != null) {
                map.put(OPENID, request.getSpecialTags().get(OPENID));
            }
            WechatUtils.ADDITIONAL.set(map);
        }
    }

    private void parseChargeResponse(final Map<String, Object> responseMap, final String tradeType, final CreatePaymentRequest request,
            final String referenceID, final CreatePaymentResponse response, final String accountName) {
        if (SUCCESS.equals(responseMap.get("return_code")) && SUCCESS.equals(responseMap.get("result_code"))) {
            String url = urlDeducer.deduce(Triple.of(request, referenceID, responseMap));

            PaymentRequest paymentRequest = buildPaymentRequest(request, referenceID, request.getChannel(),
                    request.getReferences().get(getReference(request.getReferences())), accountName, url, PaymentOperation.CHARGE.operationType());
            paymentRequest.setStatus(PaymentRequestStatus.PENDING.status());

            getPaymentRequestDAO().save(paymentRequest);
            response.setResponseCode(ResponseStatus.PROCESS_SUCCESS_CODE);
            response.setTransactionID(getTransactionID().get());
            response.setUrl(url);
        } else {
            log.warn("Erro resonse from Wechat [{}]", Jackson.json(responseMap));
            response.setResponseDescription("Fail to parse Wechat returned response");
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public RefundResponse invokeRefundOperation(final RefundRequest request) {
        RefundResponse response = new RefundResponse();
        response.setResponseCode(ResponseStatus.INTERNAL_ERROR_CODE);
        PaymentResponse chargeResponse = getLocalResponse().get();
        String accountName = getAccount().get().getAccountName();
        String appID = getAccount().get().getPrivateKey();
        String specialChannel = null;
        if (request.getSpecialTags() != null) {
            specialChannel = request.getSpecialTags().get("channel");
        }
        PaymentRequest refundRequest = buildPaymentRequest(request, request.getReferenceID(), specialChannel, request.getRefundAmount(),
                chargeResponse.getTransactionID(), accountName, PaymentOperation.REFUND.operationType());
        getPaymentRequestDAO().save(refundRequest);

        String xmlString = WechatUtils.buildRefundXMLString(appID, chargeResponse.getTransactionID(),
                getTransactionID().get(), chargeResponse.getAcknowledgedAmount(), request.getRefundAmount(),
                getAccount().get().getPublicKey());
        Pair<Boolean, Map<String, Object>> wechatResult = callAndParse(WechatConstants.REFUND_API, xmlString);
        if (!wechatResult.getLeft()) {
            log.error("Fail to parse Wechat returned response");
            response.setResponseDescription("Fail to parse Wechat returned response");
            return response;
        }

        Map<String, Object> responseMap = wechatResult.getRight();
        boolean verify = WechatSignatureHelper.signVerifyWithKey(responseMap, getAccount().get().getPublicKey());
        if (verify) {
            if ("FAIL".equals(responseMap.get("return_code")) || "FAIL".equals(responseMap.get("result_code"))) {
                response.setResponseDescription("Wechat refund failed!");
                record(getTransactionID().get(), String.format("Wechat refund failed due to [%s]", responseMap.get("err_code_des")),
                        ErrorCode.PAYMENT_FAIL.code());
                return response;
            } else {
                refundRequest.setStatus(PaymentRequestStatus.PENDING_ON_QUERY.status());
                response.setResponseCode(ResponseStatus.PROCESS_SUCCESS_CODE);
                response.setTransactionID(getTransactionID().get());
            }
            getPaymentRequestDAO().update(refundRequest);
        } else {
            response.setResponseDescription("Wechat response verification error");
            log.warn("Wechat response verification failed, something wrong happened");
        }
        return response;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CreatePaymentResponse invokeSignOperation(final CreatePaymentRequest request) {
        CreatePaymentResponse response = new CreatePaymentResponse();
        String signTransactionID = getTransactionID().get();
        String url = WechatUtils.buildSignURL(signTransactionID, signTransactionID,
                getAccount().get().getPublicKey());
        log.info(String.format("Generate Wechat sign url [%s] for customer [%s] with business [%s]", url, request.getCustomerID(), request.getBusiness()));

        PaymentRequest signRequest = buildPaymentRequest(request, getReference(request.getReferences()), request.getChannel(),
                "0", getAccount().get().getAccountName(), url, PaymentOperation.SIGN.operationType());
        signRequest.setStatus(PaymentRequestStatus.PENDING.status());
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
    public RescindResponse invokeRescindOperation(final RescindRequest request) {
        RescindResponse response = new RescindResponse();
        Timestamp now = Timestamp.valueOf(LocalDateTime.now());
        String rescindTransactionID = getTransactionID().get();
        String externalTransactionID = getLocalResponse().get().getExternalTransactionID();
        String xmlString = WechatUtils.buildRescindXMLString(externalTransactionID, getAccount().get().getPublicKey());

        String specialChannel = null;
        if (request.getSpecialTags() != null) {
            specialChannel = request.getSpecialTags().get("channel");
        }
        PaymentRequest rescindRequest = buildPaymentRequest(request, getLocalResponse().get().getReferenceID(), specialChannel, "0",
                getLocalResponse().get().getTransactionID(), StringUtils.EMPTY, PaymentOperation.RESCIND.operationType());

        Pair<Boolean, Map<String, Object>> wechatResult = callAndParse(WechatConstants.DELETE_CONTRACT_API, xmlString);
        if (!wechatResult.getLeft()) {
            log.error("Fail to parse Wechat returned response");
            response.setResponseDescription("Fail to parse Wechat returned response");
            return response;
        }

        Map<String, Object> responseMap = wechatResult.getRight();
        boolean verify = WechatSignatureHelper.signVerifyWithKey(responseMap, getAccount().get().getPublicKey());
        if (!verify) {
            log.warn("Wechat rescind response signature verification failed, response data [{}]", response);
            response.setResponseCode(ResponseStatus.INTERNAL_ERROR_CODE);
            response.setResponseDescription("Fail to connect Wechat server to send rescind request");
            return response;
        }

        boolean succeed = StringUtils.equals(SUCCESS, (String)responseMap.get("result_code"))
                && StringUtils.equals(SUCCESS, (String)responseMap.get("return_code"));

        PaymentResponse paymentResponse = PaymentResponseBuilder.builder()
                .acknowledgedAmount("0")
                .business(request.getBusiness())
                .createTime(now)
                .customerID(request.getCustomerID())
                .externalTransactionID(externalTransactionID)
                .lastUpdateTime(now)
                .operationType(PaymentOperation.RESCIND.operationType())
                .paymentMethod(getPaymentProcessorName())
                .rawResponse(Jackson.json(responseMap))
                .referenceID(getLocalResponse().get().getReferenceID())
                .status(succeed ? PaymentResponseStatus.SUCCESS.status() : PaymentResponseStatus.FAIL.status())
                .transactionID(rescindTransactionID)
                .build();
        List<PaymentResponse> responses = new LinkedList<>();
        responses.add(paymentResponse);
        getLocalResponse().get().setStatus(PaymentResponseStatus.UNVALID.status());
        responses.add(getLocalResponse().get());
        List<PaymentRequest> requests = new LinkedList<>();
        rescindRequest.setStatus(PaymentRequestStatus.PAID.status());
        requests.add(rescindRequest);
        getPaymentsDAOService().updateRequestAndResponse(requests, responses);
        if (!succeed) {
            String errorCode = String.format("Response error detai, return_msg: [%s], err_code: [%s], err_code_des: [%s]",
                    responseMap.get("return_msg"), responseMap.get("err_code"), responseMap.get("err_code_des"));
            record(rescindTransactionID, errorCode, ErrorCode.PAYMENT_FAIL.code());
            response.setResponseCode(ResponseStatus.INTERNAL_ERROR_CODE);
            response.setResponseDescription(String.format("Rescind failed due to [%s]", errorCode));
        } else {
            response.setResponseCode(ResponseStatus.PROCESS_COMPLETE_CODE);
            response.setTransactionID(rescindTransactionID);
        }
        return response;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ScheduledPayResponse invokeScheduledPayOperation(final ScheduledPayRequest request) {
        ScheduledPayResponse response = new ScheduledPayResponse();
        response.setResponseCode(ResponseStatus.INTERNAL_ERROR_CODE);
        PaymentRequest schedulePayRequest = buildPaymentRequest(request, getLocalResponse().get().getReferenceID(), StringUtils.EMPTY, request.getAmount(),
                getLocalResponse().get().getTransactionID(), StringUtils.EMPTY, PaymentOperation.SCHEDULEDPAY.operationType());
        getPaymentRequestDAO().save(schedulePayRequest);

        String xmlString = WechatUtils.buildScheduledPayXMLString(getTransactionID().get(), request.getAmount(),
                getLocalResponse().get().getExternalTransactionID(), BusinessProfile.fromProfile(request.getBusiness()).displayName(),
                getAccount().get().getPublicKey());
        Pair<Boolean, Map<String, Object>> wechatResult = callAndParse(WechatConstants.PAP_PAY_APPLY_API, xmlString);
        if (!wechatResult.getLeft()) {
            log.error("Fail to parse Wechat returned response");
            response.setResponseDescription("Fail to parse Wechat returned response");
            return response;
        }
        Map<String, Object> responseMap = wechatResult.getRight();

        boolean verify = WechatSignatureHelper.signVerifyWithKey(responseMap, getAccount().get().getPublicKey());
        if (!verify) {
            log.warn("Wechat scheduled pay response signature verification failed");
            response.setResponseCode(ResponseStatus.INTERNAL_ERROR_CODE);
            response.setResponseDescription("Fail to connect Wechat server to send scheduled pay request");
            return response;
        }

        boolean succeed = StringUtils.equals(SUCCESS, (String)responseMap.get("result_code"))
                && StringUtils.equals(SUCCESS, (String)responseMap.get("return_code"));
        String errorCode = String.format("Response error detai, return_msg: [%s], err_code: [%s], err_code_des: [%s]",
                responseMap.get("return_msg"), responseMap.get("err_code"), responseMap.get("err_code_des"));
        if (!succeed) {
            record(getTransactionID().get(), errorCode, ErrorCode.PAYMENT_FAIL.code());
            response.setResponseCode(ResponseStatus.INTERNAL_ERROR_CODE);
            response.setResponseDescription(String.format("Rescind failed due to [%s]", errorCode));
        } else {
            schedulePayRequest.setStatus(PaymentRequestStatus.PENDING.status());
            getPaymentRequestDAO().update(schedulePayRequest);
            response.setResponseCode(ResponseStatus.PROCESS_SUCCESS_CODE);
            response.setTransactionID(getTransactionID().get());
        }
        return response;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getTransactionID(final CreateOrUpdatePaymentResponseRequest request) {
        if (StringUtils.equals(request.getPaymentOperationType(), PaymentOperation.CHARGE.operationType())) {
            return request.getParameters().get("out_trade_no")[0];
        }
        if (StringUtils.equals(request.getPaymentOperationType(), PaymentOperation.REFUND.operationType())) {
            return request.getParameters().get("out_refund_no")[0];
        }
        if (StringUtils.equals(request.getPaymentOperationType(),  PaymentOperation.SIGN.operationType())) {
            return request.getParameters().get("contract_code")[0];
        }
        if (StringUtils.equals(request.getPaymentOperationType(), PaymentOperation.SCHEDULEDPAY.operationType())) {
            return request.getParameters().get("out_trade_no")[0];
        }
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getExternalTransactionID(final CreateOrUpdatePaymentResponseRequest request) {
        if (StringUtils.equals(request.getPaymentOperationType(), PaymentOperation.CHARGE.operationType())) {
            return request.getParameters().get("transaction_id")[0];
        }
        if (StringUtils.equals(request.getPaymentOperationType(), PaymentOperation.REFUND.operationType())) {
            return request.getParameters().get("refund_id")[0];
        }
        if (StringUtils.equals(request.getPaymentOperationType(), PaymentOperation.SIGN.operationType())) {
            return request.getParameters().get("contract_id")[0];
        }
        if (StringUtils.equals(request.getPaymentOperationType(), PaymentOperation.SCHEDULEDPAY.operationType())) {
            return request.getParameters().get("transaction_id")[0];
        }
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getStatus(final CreateOrUpdatePaymentResponseRequest request) {
        String resultCode = request.getParameters().get("result_code")[0];
        if (StringUtils.equals("SUCCESS", resultCode)) {
            return PaymentResponseStatus.SUCCESS.status();
        }
        return PaymentResponseStatus.FAIL.status();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String success() {
        Map<String, Object> resultMap = new HashMap<String, Object>();
        resultMap.put("return_code", "SUCCESS");
        resultMap.put("return_msg", "OK");
        return XMLUtils.mapToXmlStr(resultMap);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String fail() {
        Map<String, Object> resultMap = new HashMap<String, Object>();
        resultMap.put("return_code", "FAIL");
        resultMap.put("return_msg", "FAIL");
        return XMLUtils.mapToXmlStr(resultMap);
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

    /**
     * Call Wechat server and parse the response.
     * @param url Wechat server's HTPP URL.
     * @param content Content to be sent to Wechat.
     * @return Wechat returned response
     *      First : flag indicates if we called Wechat successfully.
     *      Second : Parsed map containing all the Wechat returned information.
     */
    private Pair<Boolean, Map<String, Object>> callAndParse(final String url, final String content) {
        Pair<Boolean, Map<String, Object>> wechatResult = Pair.of(true, null);
        Pair<String, String> pair = Pair.of(url, content);
        log.info("Send request data [{}] to wechat server [{}]", content, url);
        String result = getFacade().call(pair);
        try {
            log.info("Reveied wechat response [{}]", result);
            Map<String, Object> responseMap = XMLUtils.getMapFromXML(result);
            wechatResult = Pair.of(true, responseMap);
            // If the response does not contain the signature portion, we will bypass the verification step.
            if (responseMap.get("sign") == null) {
                wechatResult = Pair.of(false, null);
            }
        } catch (Exception e) {
            log.error("Fail to parse wechat resonse for api [{}]", url);
            wechatResult = Pair.of(false, null);
        }
        return wechatResult;
    }

    private PaymentRequest buildPaymentRequest(final Request request, final String referenceID,
            final String channel, final String requestedAmount, final String parentID,
            final String url, final String paymentOperationType) {
        Timestamp now = new Timestamp(System.currentTimeMillis());
        return PaymentRequestBuilder.builder()
                .business(request.getBusiness())
                .callBackMetaInfo(Jackson.json(request.getCallbackInfo()))
                .channel(channel)
                .createTime(now)
                .customerID(request.getCustomerID())
                .lastUpdateTime(now)
                .parentID(parentID)
                .paymentMethod(NAME)
                .paymentOperationType(paymentOperationType)
                .referenceID(referenceID)
                .requestedAmount(requestedAmount)
                .status(PaymentRequestStatus.UNVALID.status())
                .transactionID(getTransactionID().get())
                .url(url)
                .build();
    }
}
