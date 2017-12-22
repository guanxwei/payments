/**
 * 
 */
package org.wgx.payments.flowcontrol;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wgx.payments.clients.RedisClient;

import lombok.Data;

/**
 * 领奖流量控制
 * 
 *
 */
@Data
public class FlowMonitor {

    private static final Logger LOGGER= LoggerFactory.getLogger(FlowMonitor.class);

    private String key;
    private int capacity;
    private BlockingQueue<Short> queue;
    private volatile boolean isRebooting = false;

    @Resource
    private RedisClient redisService;

    // 谷歌令牌桶限流，最大允许每秒50个请求
    //private static RateLimiter rateLimiter = RateLimiter.create(50);

    public void init() {
        initiateQueue();
    }

    private void initiateQueue() {
        if (capacity > 0 && capacity < 100) {
            queue = new LinkedBlockingQueue<>(capacity);
        } else {
            queue = new LinkedBlockingQueue<>(20);
        }
    }

    public void reboot(final int capacity) {
        isRebooting = true;
        if (waitCompleted()) {
            queue = new LinkedBlockingQueue<>(getCapacity());
        }
        isRebooting = false;
    }

    private boolean waitCompleted() {
        int count = 66;
        // 防止因为处理能力有限不了等待时间太久使得请求一直得不到处理而卡死，这边最多等待2秒钟，如果还没处理完剩下的任务，直接放弃本次容量扩充处理.
        while (queue.size() > 0 && count-- > 0) {
            try {
                Thread.sleep(30);
            } catch (InterruptedException e) {
                LOGGER.info("Wait until the FlowMonitor complete its work");
            }
        }
        //已经处理完
        if (queue.size() <= 0) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * 全体请求流浪控制,这边通过入队,出队的形式加锁,也就是同一时刻最多有{@code AwardRateLimitUtil#capacity}个请求阻塞等待处理.
     * 如果当前流量监视器正在更新队列长度，则直接返回FALSE，直到之前的任务全部完成为止；一般而言，配置的任务队列长度较短，只要不是处理非常耗时
     * 的任务（耗时的任务也不建议通过本监视器拦截）.
     * @return
     */
    public boolean enqueue() {
        if (isRebooting) {
            return false;
        }
        try {
            return queue.offer(Short.MIN_VALUE, 0, TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 对应到{@code AwardRateLimitUtil#enqueue()}, 请求处理完后出队列用.
     * @return
     */
    public boolean dequeue() {
        try {
            queue.poll(300, TimeUnit.MILLISECONDS);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
