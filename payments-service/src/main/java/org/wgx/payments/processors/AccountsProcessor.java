package org.wgx.payments.processors;

import org.wgx.payments.client.api.helper.PaymentMethod;
import org.wgx.payments.client.api.io.CreateOrUpdatePaymentResponseRequest;
import org.wgx.payments.client.api.io.CreatePaymentRequest;
import org.wgx.payments.client.api.io.CreatePaymentResponse;
import org.wgx.payments.client.api.io.RefundRequest;
import org.wgx.payments.client.api.io.RefundResponse;
import org.wgx.payments.execution.SimplePaymentProcessor;

/**
 * Payment processor to support purchasing by account.
 * @author hzweiguanxiong
 *
 */
public class AccountsProcessor extends SimplePaymentProcessor {

    private static final String NAME = PaymentMethod.ACCOUNTS.name();

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
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public RefundResponse invokeRefundOperation(final RefundRequest request) {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getTransactionID(final CreateOrUpdatePaymentResponseRequest request) {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getExternalTransactionID(final CreateOrUpdatePaymentResponseRequest request) {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getStatus(final CreateOrUpdatePaymentResponseRequest request) {
        // TODO Auto-generated method stub
        return 0;
    }

}
