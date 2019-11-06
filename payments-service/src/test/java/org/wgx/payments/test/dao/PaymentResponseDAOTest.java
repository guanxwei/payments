package org.wgx.payments.test.dao;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

import java.sql.Timestamp;
import java.util.List;

import javax.annotation.Resource;

import org.testng.annotations.Test;
import org.wgx.payments.dao.PaymentResponseDAO;
import org.wgx.payments.model.PaymentResponse;
import org.wgx.payments.tools.ObjectGenerator;
import org.wgx.payments.utils.DateUtils;

public class PaymentResponseDAOTest extends DAOTestBase {

    @Resource
    private PaymentResponseDAO paymentResponseDAO;

    @Test
    public void save() throws Exception {
        PaymentResponse response = ObjectGenerator.generate(PaymentResponse.class);
        assertEquals(paymentResponseDAO.save(response), 1);
    }

    @Test
    public void getPaymentResponseListByRange() throws Exception {
        PaymentResponse response = ObjectGenerator.generate(PaymentResponse.class);
        assertEquals(paymentResponseDAO.save(response), 1);
        Timestamp begin = new Timestamp(System.currentTimeMillis() - 100000);
        Timestamp end = new Timestamp(System.currentTimeMillis() + 100000);
        List<PaymentResponse> responses = paymentResponseDAO.getPaymentResponseListByRange(DateUtils.convertFromTimestamp(begin),
                DateUtils.convertFromTimestamp(end));
        assertNotNull(responses);
        assertTrue(responses.size() >= 1);
    }
}
