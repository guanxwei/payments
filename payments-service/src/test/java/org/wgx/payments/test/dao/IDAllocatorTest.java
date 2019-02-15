package org.wgx.payments.test.dao;

import static org.testng.Assert.assertEquals;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicLong;

import javax.annotation.Resource;

import org.testng.annotations.Test;
import org.wgx.payments.transaction.TransactionManager;

public class IDAllocatorTest extends DAOTestBase {

    @Resource
    private TransactionManager transactionManager;

    @Test
    public void testParalleAllocate() throws Throwable {
        int threads = Runtime.getRuntime().availableProcessors();
        int counts = 10000;
        String table = "CheckOrderDiffItem";
        Set<Long> sets = new HashSet<Long>();
        CountDownLatch countDownLatch = new CountDownLatch(threads * counts);
        AtomicLong allocateds = new AtomicLong();

        for (int i = 0; i < threads; i++) {
            Thread thread = new Thread(() -> {
                for (int j = 0; j < counts; j++) {
                    long id = transactionManager.allocateID(table);
                    boolean added = sets.add(id);
                    if (!added) {
                        System.out.println(id);
                    }
                    allocateds.incrementAndGet();
                    countDownLatch.countDown();
                }
            });
            thread.start();
        }
        countDownLatch.await();
        assertEquals(allocateds.longValue(), sets.size());
    }
}
