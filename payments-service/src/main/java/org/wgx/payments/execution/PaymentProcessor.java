package org.wgx.payments.execution;

import org.wgx.payments.client.api.helper.PaymentMethod;
import org.wgx.payments.client.api.helper.PaymentOperation;
import org.wgx.payments.client.api.io.CreateOrUpdatePaymentResponseRequest;
import org.wgx.payments.client.api.io.CreateOrUpdatePaymentResponseResponse;
import org.wgx.payments.client.api.io.CreatePaymentRequest;
import org.wgx.payments.client.api.io.CreatePaymentResponse;
import org.wgx.payments.client.api.io.QueryPaymentTransactionRequest;
import org.wgx.payments.client.api.io.QueryPaymentTransactionResponse;
import org.wgx.payments.client.api.io.RefundRequest;
import org.wgx.payments.client.api.io.RefundResponse;
import org.wgx.payments.client.api.io.Request;
import org.wgx.payments.client.api.io.RescindRequest;
import org.wgx.payments.client.api.io.RescindResponse;
import org.wgx.payments.client.api.io.Response;
import org.wgx.payments.client.api.io.ScheduledPayRequest;
import org.wgx.payments.client.api.io.ScheduledPayResponse;

import com.google.common.collect.ImmutableMap;

/**
 * Encapsulation of payment processor. Each payment processor will take the responsibility of
 * handling the payment logic for exactly one payment method.
 *
 */
public interface PaymentProcessor {

    /**
     * Service interfaces' request to response type mapping table.
     */
    ImmutableMap<Class<?>, Class<?>> REQUEST_TO_RESPONSE_MAPPING = ImmutableMap.<Class<?>, Class<?>>builder()
            .put(CreatePaymentRequest.class, CreatePaymentResponse.class)
            .put(RefundRequest.class, RefundResponse.class)
            .put(RescindRequest.class, RescindResponse.class)
            .put(QueryPaymentTransactionRequest.class, QueryPaymentTransactionResponse.class)
            .put(ScheduledPayRequest.class, ScheduledPayResponse.class)
            .put(CreateOrUpdatePaymentResponseRequest.class, CreateOrUpdatePaymentResponseResponse.class)
            .build();

    /**
     * Payment operation type to code mapping.
     */
    ImmutableMap<String, String> PAYMENT_OPERATION_CODES = ImmutableMap.<String, String>builder()
            .put(PaymentOperation.CHARGE.operationType(), "01")
            .put(PaymentOperation.REFUND.operationType(), "02")
            .put(PaymentOperation.RESCIND.operationType(), "03")
            .put(PaymentOperation.SCHEDULEDPAY.operationType(), "04")
            .put(PaymentOperation.SIGN.operationType(), "05")
            .build();

    /**
     * Payment method name to code mapping.
     */
    ImmutableMap<String, String> PAYMENT_METHOD_CODES = ImmutableMap.<String, String>builder()
            .put(PaymentMethod.ALIPAY.paymentMethodName(), "01")
            .put(PaymentMethod.WECHAT.paymentMethodName(), "02")
            .put(PaymentMethod.IAP.paymentMethodName(), "03")
            .build();

    /**
     * Get the payment processor's name. The name should be unique in the context.
     * @return The payment processor's name.
     */
    String getPaymentProcessorName();

    /**
     * Process the input request.
     *
     * @param t Input request.
     * @return Processed response.
     */
    Response processRequest(final Request t);

    /**
     * Process the returned payment response(pushed back by 3P payment gateway).
     *
     * @param t Returned payment response.
     * @return Processing status.
     */
    Response processResponse(final Request t);
}
