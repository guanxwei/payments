package org.wgx.payments.test.dao;

import static org.testng.Assert.assertEquals;

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
        int threads = 10;
        int counts = 100;
        String table = "TestAllocateTable";
        CountDownLatch countDownLatch = new CountDownLatch(threads * counts);
        AtomicLong allocateds = new AtomicLong();

        for (int i = 0; i < threads; i++) {
            Thread thread = new Thread(() -> {
                for (int j = 0; j < counts; j++) {
                    transactionManager.allocateID(table);
                    allocateds.incrementAndGet();
                    countDownLatch.countDown();
                }
            });
            thread.start();
        }
        countDownLatch.await();
        assertEquals(allocateds.longValue(), 1000);
    }
}
