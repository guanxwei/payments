package org.wgx.payments.validator;

import javax.annotation.Resource;

import org.apache.commons.lang3.tuple.Pair;
import org.stream.extension.clients.RedisClient;
import org.wgx.payments.client.api.io.CreateOrUpdatePaymentResponseRequest;
import org.wgx.payments.facade.Facade;
import org.wgx.payments.tools.Jackson;
import org.wgx.payments.utils.IAPConstants;

import lombok.Setter;

/**
 * Apple IAP payment processor.
 *
 */
public class IAPValidator implements Validator<CreateOrUpdatePaymentResponseRequest> {

    // CHECKSTYLE:OFF
    public static final String IAP_VALIDATION_FAILED_TRANSACTION_PREFIX = "IAP::Retry::";
    public static final String IAP_VALIDATION_FAILED_LIST = "Payments_IAP_Validation_Fail_List";
    // CHECKSTYLE:ON

    @Resource(name = "IAPFacade")
    @Setter
    private Facade<Pair<String, String>, Boolean> iapFacade;

    @Resource
    @Setter
    private RedisClient redisService;

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean validate(final CreateOrUpdatePaymentResponseRequest request) {
        String receipt = request.getParameters().get("receipt")[0];
        String transactionID = request.getParameters().get("transactionID")[0];
        String url = IAPConstants.VERIFY_URL;
        if (request.getParameters().containsKey("sandbox")) {
            if ("sandbox".equals(request.getParameters().get("env")[0])) {
                url = IAPConstants.SAND_BOX_URL;
            } else {
                url = IAPConstants.ONLINE_URL;
            }
        }
        if (checkBaseInfo(receipt, transactionID)) {
            Pair<String, String> pair = Pair.of(url, receipt);
            try {
                if (iapFacade.call(pair)) {
                    return true;
                }
            } catch (Exception e) {
                // 在Redis中缓存一天，超过一天还是不能处理，抛弃之.
                redisService.sadd(IAP_VALIDATION_FAILED_LIST, transactionID);
                redisService.setWithExpireTime(IAP_VALIDATION_FAILED_TRANSACTION_PREFIX + transactionID, Jackson.json(request), 24 * 3600);
                return false;
            }
        }
        return false;
    }

    private boolean checkBaseInfo(final String receipt, final String transactionID) {
        if (receipt == null || receipt.length() == 0) {
            return false;
        }
        if (transactionID == null || transactionID.length() == 0) {
            return false;
        }
        return true;
    }
}
