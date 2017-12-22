package org.wgx.payments.controller;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.wgx.payments.client.api.Activity;
import org.wgx.payments.client.api.helper.PaymentMethod;
import org.wgx.payments.client.api.helper.PaymentOperation;
import org.wgx.payments.client.api.io.CreateOrUpdatePaymentResponseRequest;
import org.wgx.payments.client.api.io.CreateOrUpdatePaymentResponseResponse;
import org.wgx.payments.execution.PaymentProcessorManager;
import org.wgx.payments.utils.XMLUtils;

import com.google.common.collect.ImmutableList;

import lombok.extern.slf4j.Slf4j;

/**
 * <p>Payments platform's call back controller.
 *
 * This controller only helps listen and receive the raw HTTP requests from the 3P payment gateway
 * then assemble them into payments platform required request structure.
 *
 * It will call the payments platform's related service to handle the response.
 *
 */
@RestController
@RequestMapping(path = "/music/payments/callback")
@Slf4j
public class NotifyURLController {

    private static final ImmutableList<String> XML_RESPONSE_METHODS = ImmutableList.<String>builder()
            .add(PaymentMethod.WECHAT.paymentMethodName())
            .build();

    private static final String ERROR = "error";

    @Resource
    private PaymentProcessorManager paymentProcessorManager;

    @Resource(name = "createOrUpdatePaymentResponseService")
    private Activity<CreateOrUpdatePaymentResponseRequest, CreateOrUpdatePaymentResponseResponse> activity;

    /**
     * Payments platform's call back handler.
     * @param request Raw HTTP request from 3P gateway.
     * @param paymentMethod Payment method.
     * @param operationType Payment operation type.
     * @return Process response will be sent back to 3P gateway.
     */
    @RequestMapping(path = "/{paymentMethod}/{operationType}", method = {RequestMethod.GET, RequestMethod.POST})
    public String callback(final HttpServletRequest request, @PathVariable("paymentMethod") final String paymentMethod,
            @PathVariable("operationType") final String operationType) {
        log.info(String.format("Receive response for payment method : [%s], payment operation type : [%s]",
                paymentMethod, operationType));
        if (validateBasicInfo(request, paymentMethod, operationType)) {
            CreateOrUpdatePaymentResponseResponse response;
            CreateOrUpdatePaymentResponseRequest requestWrapper = new CreateOrUpdatePaymentResponseRequest();
            if (XML_RESPONSE_METHODS.contains(paymentMethod)) {
                if (!wrap(request, requestWrapper, paymentMethod, operationType)) {
                    return "error";
                }
            } else {
                requestWrapper.setParameters(request.getParameterMap());
                requestWrapper.setQueryString(request.getQueryString());
            }
            requestWrapper.setPaymentMethodName(paymentMethod);
            requestWrapper.setPaymentOperationType(operationType);
            response = activity.execute(requestWrapper);
            return response.getResponseDescription();
        }
        return ERROR;
    }

    private boolean validateBasicInfo(final HttpServletRequest request, final String paymentMethod, final String operationType) {
        if (paymentProcessorManager.retrievePaymentProcessor(paymentMethod) == null) {
            log.warn("Payment processor does not exist, unvalid request: [{}]", request);
            return false;
        }
        if (PaymentOperation.fromString(operationType) == null) {
            log.warn("Payment operation not supported, unvalid request: [{}]", request);
            return false;
        }
        return true;
    }

    private boolean wrap(final HttpServletRequest request, final CreateOrUpdatePaymentResponseRequest requestWrapper, final String paymentMethod,
            final String operationType) {
        try (InputStream inStream = request.getInputStream();
            ByteArrayOutputStream outSteam = new ByteArrayOutputStream();) {
            byte[] buffer = new byte[2048];
            int len;
            while ((len = inStream.read(buffer)) != -1) {
                outSteam.write(buffer, 0, len);
            }
            outSteam.close();
            inStream.close();
            String result  = new String(outSteam.toByteArray(), "utf-8");
            log.info(String.format("Received XML response [%s] from [%s] for operation [%s]", result, paymentMethod, operationType));
            Map<String, Object> params = XMLUtils.getMapFromXML(result);
            Map<String, String[]> parameters = new HashMap<>();
            params.keySet().forEach(key -> {
                String value = (String) params.get(key);
                String[] values = new String[] {value};
                parameters.put(key, values);
            });
            requestWrapper.setParameters(parameters);
            return true;
        } catch (Exception e) {
            log.error("Fail to process XML response", e);
            return false;
        }
    }
}
