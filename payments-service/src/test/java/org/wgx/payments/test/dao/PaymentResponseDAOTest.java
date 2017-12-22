package org.wgx.payments.test.dao;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

import java.sql.Timestamp;
import java.util.List;

import javax.annotation.Resource;

import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.Test;
import org.wgx.payments.dao.DAOConfiguration;
import org.wgx.payments.dao.PaymentResponseDAO;
import org.wgx.payments.model.PaymentResponse;
import org.wgx.payments.tools.ObjectGenerator;
import org.wgx.payments.utils.DateUtils;

@ContextConfiguration(classes = {
        DAOConfiguration.class,
        DaoTestConfiguration.class
    })
public class PaymentResponseDAOTest extends AbstractTestNGSpringContextTests {

    @Resource
    private PaymentResponseDAO paymentResponseDAO;

    @Test
    public void testgetPaymentResponseByReferenceIDAndBusiness() throws Exception {
        PaymentResponse response = ObjectGenerator.generate(PaymentResponse.class);
        assertEquals(paymentResponseDAO.save(response), 1);
        List<PaymentResponse> responses = paymentResponseDAO.getPaymentResponseByReferenceIDAndBusiness(
                response.getReferenceID(), response.getBusiness());
        assertNotNull(responses);
        assertEquals(responses.size(), 1);
    }

    @Test
    public void save() throws Exception {
        PaymentResponse response = ObjectGenerator.generate(PaymentResponse.class);
        assertEquals(paymentResponseDAO.save(response), 1);
    }

    @Test
    public void update() throws Exception {
        PaymentResponse response = ObjectGenerator.generate(PaymentResponse.class);
        assertEquals(paymentResponseDAO.save(response), 1);
        long now = System.currentTimeMillis() + 110000;
        response.setStatus(2000);
        response.setLastUpdateTime(new Timestamp(now));
        assertEquals(paymentResponseDAO.update(response), 1);
        PaymentResponse result = paymentResponseDAO.getPaymentResponseByTransactionID(response.getTransactionID());
        assertEquals(result.getAcknowledgedAmount(), response.getAcknowledgedAmount());
        assertEquals(result.getBusiness(), response.getBusiness());
        assertEquals(response.getCreateTime(), result.getCreateTime());
        assertEquals(result.getCustomerID(), response.getCustomerID());
        assertEquals(result.getExternalTransactionID(), response.getExternalTransactionID());
        assertEquals(result.getLastUpdateTime().getTime(), now);
        assertEquals(result.getOperationType(), response.getOperationType());
        assertEquals(result.getPaymentMethod(), response.getPaymentMethod());
        assertEquals(result.getRawResponse(), response.getRawResponse());
        assertEquals(result.getTransactionID(), response.getTransactionID());
    }

    @Test
    public void getPaymentResponseListByCustomerIDAndOperationType() throws Exception {
        PaymentResponse response = ObjectGenerator.generate(PaymentResponse.class);
        assertEquals(paymentResponseDAO.save(response), 1);
        List<PaymentResponse> responses = paymentResponseDAO.getPaymentResponseListByCustomerIDAndOperationType(
                response.getCustomerID(), response.getOperationType());
        assertNotNull(responses);
        assertEquals(responses.size(), 1);
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
