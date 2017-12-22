package org.wgx.payments.callback;

import org.wgx.payments.client.api.io.CallbackMetaInfo;

/**
 * Base interface of callback infrastructure.
 *
 * We only define the IO types of this interface, customers can implement this interface using different strategies
 * only if that can fulfill their requirement.
 *
 * Payments will use one of its concrete implementation classes to notify upstream services when it is needed.
 * Currently the combination of {@linkplain CallbackProxy} and {@linkplain HttpBasedCallback} is used to push
 * back message to Payments-platform's internal upstream clients.
 * {@link SimplePaymentProcessor} and some other back-end jobs like {@linkplain WechatQueryRefundJob} have adopted
 * this strategy, for detail please refer to the code in these classes.
 *
 * The brief procedure is as below:
 * (1) Call {@link CallbackProxy} first to save an callback instance in memory and DB(in case of OS crash),
 *          once done, {@linkplain CallbackProxy} will return succeed directly to caller.
 * (2) {@linkplain BackendCallbackJob} retrieve callback instance one by one from the memory storage then call
 *         {@linkplain HttpBasedCallback} to do the real job.
 *
 */
@FunctionalInterface
public interface Callback {

    /**
     * Call other services according to call back meta information.
     * @param info Call back information.
     * @return Result.
     */
    CallbackDetail call(final CallbackMetaInfo info);

}
