package org.wgx.payments.controller;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.wgx.payments.builder.PaymentRequestBuilder;
import org.wgx.payments.builder.PaymentResponseBuilder;
import org.wgx.payments.client.api.helper.PaymentMethod;
import org.wgx.payments.client.api.helper.PaymentOperation;
import org.wgx.payments.client.api.helper.PaymentResponseStatus;
import org.wgx.payments.client.api.io.CreateOrUpdatePaymentResponseRequest;
import org.wgx.payments.dao.PaymentRequestDAO;
import org.wgx.payments.dao.PaymentResponseDAO;
import org.wgx.payments.dao.service.PaymentsDAOService;
import org.wgx.payments.execution.PaymentProcessorManager;
import org.wgx.payments.execution.SimplePaymentProcessor;
import org.wgx.payments.model.PaymentRequest;
import org.wgx.payments.model.PaymentRequestStatus;
import org.wgx.payments.model.PaymentResponse;
import org.wgx.payments.processors.AlipayProcessor;
import org.wgx.payments.processors.WechatProcessor;
import org.wgx.payments.tools.Jackson;
import org.wgx.payments.utils.XMLUtils;

import com.google.common.collect.ImmutableMap;

import lombok.extern.slf4j.Slf4j;

/**
 * Back-end callback handler for passive notification.
 * Originally, it is designed for Alipay and Wechat rescind scenarios.
 * When customers rescind contract in Alipay and Wechat APPs, in our side, there will be no rescind request records,
 * so the {@link NotifyURLController} can not work properly for such cases(that handler works based on the existed
 * payment request).
 *
 * Also, we should keep in mind that this controller's URL is manually configured in Alipay and Wechat systems, once
 * we decide to migrate the Payments-Platform, we'll have to update the URL in their systems.
 *
 */
@RestController
@RequestMapping(path = "/music/payments/callback/passive")
@Slf4j
public class PassiveRescindNotifyController {

    private static final ImmutableMap<String, String> SUCCESS_MAP = ImmutableMap.<String, String>builder()
            .put(PaymentMethod.ALIPAY.paymentMethodName(), "SUCCESS")
            .put(PaymentMethod.WECHAT.paymentMethodName(), success())
            .build();

    private static final ImmutableMap<String, String> FAIL_MAP = ImmutableMap.<String, String>builder()
            .put(PaymentMethod.ALIPAY.paymentMethodName(), "FAIL")
            .put(PaymentMethod.WECHAT.paymentMethodName(), fail())
            .build();

    @Resource(name = "paymentProcessorManager")
    private PaymentProcessorManager paymentProcessorManager;

    @Resource(name = "paymentRequestDAO")
    private PaymentRequestDAO paymentRequestDAO;

    @Resource(name = "paymentResponseDAO")
    private PaymentResponseDAO paymentResponseDAO;

    @Resource(name = "paymentsDAOService")
    private PaymentsDAOService paymentsDAOService;

    /**
     * Passive rescind response listener.
     * @param request Http request from 3P payment gateway system.
     * @param paymentMethod The payment method corresponding to the response.
     * @return Processing result.
     */
    @RequestMapping(path = "/{paymentMethod}")
    public String callback(final HttpServletRequest request, @PathVariable("paymentMethod") final String paymentMethod) {
        log.info("Receive back-end passive notification from [{}]", paymentMethod);
        if (PaymentMethod.ALIPAY.paymentMethodName().equals(paymentMethod)) {
            AlipayProcessor processor = (AlipayProcessor) paymentProcessorManager.retrievePaymentProcessor(paymentMethod);
            return handleAlipayResponse(request, processor);
        } else if (PaymentMethod.WECHAT.paymentMethodName().equals(paymentMethod)) {
            WechatProcessor processor = (WechatProcessor) paymentProcessorManager.retrievePaymentProcessor(paymentMethod);
            return handleWechatResponse(request, processor);
        } else {
            return null;
        }
    }

    private String handleAlipayResponse(final HttpServletRequest request, final AlipayProcessor processor) {
        String signRequestTransactionID = request.getParameter("external_sign_no");
        PaymentRequest signRequest = paymentRequestDAO.getPaymentRequestByTransactionID(signRequestTransactionID);
        PaymentResponse signResponse = paymentResponseDAO.getPaymentResponseByTransactionID(signRequestTransactionID);
        CreateOrUpdatePaymentResponseRequest requestWrapper = new CreateOrUpdatePaymentResponseRequest();
        requestWrapper.setParameters(request.getParameterMap());
        requestWrapper.setQueryString(request.getQueryString());
        requestWrapper.setPaymentOperationType(PaymentOperation.RESCIND.operationType());
        return update(signRequest, signResponse, processor, request, requestWrapper);
    }

    private String handleWechatResponse(final HttpServletRequest request, final WechatProcessor processor) {
        CreateOrUpdatePaymentResponseRequest requestWrapper = new CreateOrUpdatePaymentResponseRequest();
        String signRequestTransactionID = null;
        String result = null;
        try (InputStream inStream = request.getInputStream();
            ByteArrayOutputStream outSteam = new ByteArrayOutputStream();) {
            byte[] buffer = new byte[2048];
            int len;
            while ((len = inStream.read(buffer)) != -1) {
                outSteam.write(buffer, 0, len);
            }
            outSteam.close();
            inStream.close();
            result  = new String(outSteam.toByteArray(), "utf-8");
            log.info(String.format("Received backend passive XML response [%s] from [%s] for operation [%s]", result,
                    processor.getPaymentProcessorName(), PaymentOperation.RESCIND.operationType()));
            Map<String, Object> params = XMLUtils.getMapFromXML(result);
            Map<String, String[]> parameters = new HashMap<>();
            params.keySet().forEach(key -> {
                String value = (String) params.get(key);
                String[] values = new String[] {value};
                parameters.put(key, values);
            });
            requestWrapper.setParameters(parameters);
            requestWrapper.setPaymentOperationType(PaymentOperation.RESCIND.operationType());
            signRequestTransactionID = (String) params.get("contract_code");
        } catch (Exception e) {
            log.error("Fail to process XML response", e);
            return "error";
        }
        PaymentRequest signRequest = paymentRequestDAO.getPaymentRequestByTransactionID(signRequestTransactionID);
        PaymentResponse signResponse = paymentResponseDAO.getPaymentResponseByTransactionID(signRequestTransactionID);
        return update(signRequest, signResponse, processor, request, requestWrapper);
    }

    private static String success() {
        Map<String, Object> resultMap = new HashMap<String, Object>();
        resultMap.put("return_code", "SUCCESS");
        resultMap.put("return_msg", "OK");
        return XMLUtils.mapToXmlStr(resultMap);
    }

    private static String fail() {
        Map<String, Object> resultMap = new HashMap<String, Object>();
        resultMap.put("return_code", "FAIL");
        resultMap.put("return_msg", "FAIL");
        return XMLUtils.mapToXmlStr(resultMap);
    }

    private String update(final PaymentRequest signRequest, final PaymentResponse signResponse,
            final SimplePaymentProcessor processor, final HttpServletRequest request, final CreateOrUpdatePaymentResponseRequest requestWrapper) {
        Timestamp now = Timestamp.valueOf(LocalDateTime.now());
        PaymentRequest passiveRescindRequest = PaymentRequestBuilder.builder()
                .business(signRequest.getBusiness())
                .callBackMetaInfo(null)
                .channel("passive")
                .createTime(now)
                .customerID(signRequest.getCustomerID())
                .lastUpdateTime(now)
                .parentID(signRequest.getTransactionID())
                .paymentMethod(processor.getPaymentProcessorName())
                .paymentOperationType(PaymentOperation.RESCIND.operationType())
                .referenceID(signRequest.getReferenceID())
                .requestedAmount("0")
                .status(PaymentRequestStatus.PAID.status())
                .url(null)
                .transactionID(processor.getSignatureGenerator().generate())
                .build();
        PaymentResponse passiveRescindResponse = PaymentResponseBuilder.builder()
                .acknowledgedAmount("0")
                .business(signRequest.getBusiness())
                .createTime(now)
                .customerID(signRequest.getCustomerID())
                .externalTransactionID(signResponse.getExternalTransactionID())
                .lastUpdateTime(now)
                .operationType(PaymentOperation.RESCIND.operationType())
                .paymentMethod(processor.getPaymentProcessorName())
                .rawResponse(Jackson.json(requestWrapper.getParameters()))
                .referenceID(signRequest.getReferenceID())
                .status(PaymentResponseStatus.SUCCESS.status())
                .transactionID(passiveRescindRequest.getTransactionID())
                .build();
        boolean validationResult = processor.getValidator().validate(requestWrapper);
        if (validationResult) {
            List<PaymentRequest> requests = new LinkedList<>();
            requests.add(passiveRescindRequest);
            List<PaymentResponse> responses = new LinkedList<>();
            signResponse.setLastUpdateTime(now);
            signResponse.setStatus(PaymentResponseStatus.UNVALID.status());
            responses.add(passiveRescindResponse);
            responses.add(signResponse);
            paymentsDAOService.updateRequestAndResponse(requests, responses);
            return SUCCESS_MAP.get(processor.getPaymentProcessorName());
        } else {
            return FAIL_MAP.get(processor.getPaymentProcessorName());
        }
    }
}
