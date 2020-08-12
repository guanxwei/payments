package org.wgx.payments.test.dao;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import java.sql.Timestamp;
import java.util.List;

import javax.annotation.Resource;

import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.Test;
import org.wgx.payments.builder.ActionRecordBuilder;
import org.wgx.payments.dao.ActionRecordDAO;
import org.wgx.payments.dao.DAOConfiguration;
import org.wgx.payments.model.ActionRecord;

@ContextConfiguration(classes = {
    DAOConfiguration.class,
    DaoTestConfiguration.class
})
public class DaoFrameworkTest extends AbstractTestNGSpringContextTests {

    @Resource
    private ActionRecordDAO actionRecordDAO;
    @Resource
    private TestDaoService testDaoService;

    @Test
    public void testDao() {
        ActionRecord record = ActionRecordBuilder.builder()
                .errorCode(400)
                .message("test1")
                .time(new Timestamp(System.currentTimeMillis()))
                .transactionID(RandomStringUtils.randomAlphabetic(20))
                .id(System.nanoTime())
                .build();

        actionRecordDAO.record(record);
        List<ActionRecord> records = actionRecordDAO.queryByTransactionID(record.getTransactionID());
        assertEquals(records.size(), 1);

        assertEquals(records.get(0).getTime(), record.getTime());
    }

    @Test
    public void testTransaction() {
        ActionRecord record = ActionRecordBuilder.builder()
                .errorCode(400)
                .message("test2")
                .time(new Timestamp(System.currentTimeMillis()))
                .transactionID(RandomStringUtils.randomAlphabetic(20))
                .id(System.nanoTime())
                .build();

        try {
            testDaoService.insert(record);
        } catch (Exception e) {
            assertTrue(e instanceof RuntimeException);
        }
        List<ActionRecord> records = actionRecordDAO.queryByTransactionID(record.getTransactionID());

        assertEquals(records.size(), 0);

        record = ActionRecordBuilder.builder()
                .errorCode(400)
                .message("test3")
                .time(new Timestamp(System.currentTimeMillis()))
                .transactionID(RandomStringUtils.randomAlphabetic(20))
                .id(System.nanoTime())
                .build();
        testDaoService.insertWithoutError(record);
        records = actionRecordDAO.queryByTransactionID(record.getTransactionID());
        assertEquals(records.size(), 1);

        record = ActionRecordBuilder.builder()
                .errorCode(400)
                .message("test4")
                .time(new Timestamp(System.currentTimeMillis()))
                .transactionID(RandomStringUtils.randomAlphabetic(20))
                .id(System.nanoTime())
                .build();
        actionRecordDAO.record(record);
        records = actionRecordDAO.queryByTransactionID(record.getTransactionID());
        assertEquals(records.size(), 1);
        assertEquals(records.get(0).getTime(), record.getTime());
    }
}
