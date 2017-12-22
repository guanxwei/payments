package org.wgx.payments.job;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import javax.annotation.Resource;

import org.wgx.payments.callback.Callback;
import org.wgx.payments.callback.CallbackDetail;
import org.wgx.payments.callback.CallbackEvent;
import org.wgx.payments.callback.CallbackProxy;
import org.wgx.payments.client.api.io.CallbackMetaInfo;
import org.wgx.payments.clients.RedisClient;
import org.wgx.payments.dao.ActionRecordDAO;
import org.wgx.payments.model.FastSearchTableItem;
import org.wgx.payments.tools.Jackson;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

/**
 * Back end callback job to send callback notification message.
 *
 */
@Slf4j
@Setter
public class BackendCallbackJob {

    @Setter
    @Resource(name = "actionRecordDAO")
    private ActionRecordDAO actionRecordDAO;

    @Setter
    @Resource(name = "realCallback")
    private Callback callback;

    @Setter
    @Resource(name = "redisService")
    private RedisClient redisService;

    private int delay = 60;

    /**
     * Failure counter.
     */
    public static final ConcurrentHashMap<String, Integer> FAILURE_COUNTER = new ConcurrentHashMap<>();

    //private int period = 300;

    private AtomicLong count = new AtomicLong(0);

    private ScheduledExecutorService timer = Executors.newScheduledThreadPool(1);

    private ExecutorService service = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

    /**
     * Job initiate method.
     */
    public void start() {
        RedisBasedCallbackRunner runner = new RedisBasedCallbackRunner();
        timer.schedule(runner, delay, TimeUnit.SECONDS);
    }

    /**
     * DB based callback back-end runner to retrieve pending on callback notify jobs and submit them to execution workers.
     *
     */
    public class RedisBasedCallbackRunner implements Runnable {
        @Override
        public void run() {
            log.info("Redis Based Callback Runner start to execute callback notify job at time [{}]", Timestamp.valueOf(LocalDateTime.now()));
            List<String> keys = redisService.lrange(CallbackProxy.KEY_SET, 0, 100);
            for (String key : keys) {
                DBBasedWorkerRunner worker = new DBBasedWorkerRunner(key);
                try {
                    service.submit(worker);
                } catch (Exception e) {
                    log.warn("Fail to submit callback task to the executor service, quiting... Will try again later");
                }
            }
        }
    }

    /**
     * DB based Back end worker to execute callback notify jobs.
     *
     */
    public class DBBasedWorkerRunner implements Runnable {

        private String key;

        /**
         * Constructor.
         * @param item Pending on processed item.
         */
        public DBBasedWorkerRunner(final String key) {
            this.key = key;
        }

        @Override
        public void run() {
            String content = redisService.get(key);
            if (content == null) {
                redisService.lrem(CallbackProxy.KEY_SET, 1, key);
                return;
            }
            CallbackEvent callbackEvent = Jackson.parse(content, CallbackEvent.class);
            FastSearchTableItem item = Jackson.parse(callbackEvent.getDetail(), FastSearchTableItem.class);

            String transactionID = item.getTransactionID();
            boolean canBeProcessed = callbackEvent.getTimes() < 50 && redisService.setnx(key + "_lock", "a") == 1L;
            if (canBeProcessed) {
                log.info("No contention, callback with transactionID [{}] will be handled in this machine.", transactionID);
                CallbackMetaInfo info = Jackson.parse(item.getMessage(), CallbackMetaInfo.class);
                CallbackDetail detail = callback.call(info);
                boolean succeed = detail.isSucceed();
                // We will not retry even if the clients return FAIL..
                if (succeed || detail.getError().equals("FAIL")) {
                    onSucceed(key);
                } else {
                    if (callbackEvent.getTimes() >= 100) {
                        onSucceed(key);
                    } else {
                        callbackEvent.setTimes(callbackEvent.getTimes() + 1);
                        redisService.set(key, Jackson.json(callbackEvent));
                    }
                }
            }
        }
    }

    private void onSucceed(final String key) {
        redisService.del(key);
        redisService.lrem(CallbackProxy.KEY_SET, 1, key);
        log.info("No.{} item been processed", count.get());
    }
}
