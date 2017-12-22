package org.wgx.payments.callback;

import java.sql.Timestamp;

import javax.annotation.Resource;

import org.wgx.payments.builder.FastSearchTableItemBuilder;
import org.wgx.payments.client.api.io.CallbackMetaInfo;
import org.wgx.payments.clients.RedisClient;
import org.wgx.payments.model.FastSearchTableItem;
import org.wgx.payments.tools.Jackson;

import lombok.Data;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

/**
 * Proxy implementation of {@linkplain Callback}, mainly used to push the incoming request to the waiting queue and backup in Redis.
 * The real job will be executed by the back-end callback workers asynchronously.
 *
 */
@Slf4j
@Data
public class CallbackProxy implements Callback {

    /**
     * Redis key name.
     */
    public static final String KEY_SET = "Payments_Callback_Backup_KeySet";

    @Setter
    @Resource(name = "redisService")
    private RedisClient redisService;

    /**
     * {@inheritDoc}
     */
    @Override
    public CallbackDetail call(final CallbackMetaInfo info) {
        log.info("Incoming callback request to [{}]", info.getCallBackUrl());
        return invokeV1(info);
    }

    /**
     * Let's use memory storage based callback implementation.
     * Update 2017/07/27 uses RedisService to back up callback items.
     * @param info Callback information.
     * @return Callback detail.
     */
    private CallbackDetail invokeV1(final CallbackMetaInfo info) {
        log.info("Incoming callback request [{}] to be handled by Memory storate based mechanism", info.getCallBackUrl());

        FastSearchTableItem item = FastSearchTableItemBuilder.builder()
                .itemKey("callback_" + info.getParameters().get("transactionID"))
                .time(new Timestamp(System.currentTimeMillis()))
                .status(CallbackStatus.PENDING.status())
                .transactionID(info.getParameters().get("transactionID"))
                .message(Jackson.json(info))
                .build();
        CallbackEvent callbackEvent = new CallbackEvent();
        callbackEvent.setDetail(Jackson.json(item));
        boolean succeed = redisService.lpush(KEY_SET, info.getParameters().get("transactionID") + "_callback") == 1L
                && redisService.set(info.getParameters().get("transactionID") + "_callback", Jackson.json(callbackEvent));
        CallbackDetail detail = new CallbackDetail();
        detail.setSucceed(true);
        check(succeed, detail);
        if (!succeed) {
            return detail;
        }

        return detail;
    }

    private void check(final boolean succeed, final CallbackDetail detail) {
        if (!succeed) {
            detail.setSucceed(false);
            detail.setError("Redis is busy, fail to store callback meta information.");
        }
    }
}
