package org.wgx.payments.execution;

import java.util.HashMap;
import java.util.Map;

import lombok.extern.slf4j.Slf4j;

/**
 * Payment processor manager used to manage payment processors.
 *
 */
@Slf4j
public class PaymentProcessorManager {

    private Map<String, PaymentProcessor> processors = new HashMap<>();

    /**
     * Register the processor.
     * @param processor Processor to be registered.
     */
    public void registerProcessor(final PaymentProcessor processor) {
        if (!processors.containsKey(processor.getPaymentProcessorName())) {
            processors.put(processor.getPaymentProcessorName(), processor);
        } else {
            log.info("Try to replace existing payment processor, ignore it!");
        }
    }

    /**
     * Retrieve a payment processor by the processor name.
     * @param processorName The payment processor's name.
     * @return Retrieved payment processor.
     */
    public PaymentProcessor retrievePaymentProcessor(final String processorName) {
        return processors.get(processorName);
    }
}
