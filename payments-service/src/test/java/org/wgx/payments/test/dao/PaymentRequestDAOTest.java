package org.wgx.payments.test.dao;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

import java.sql.Timestamp;

import javax.annotation.Resource;

import org.testng.annotations.Test;
import org.wgx.payments.dao.PaymentRequestDAO;
import org.wgx.payments.model.PaymentRequest;
import org.wgx.payments.tools.ObjectGenerator;

public class PaymentRequestDAOTest extends DAOTestBase {

    @Resource
    private PaymentRequestDAO paymentRequestDAO;

    @Test
    public void testgetPaymentRequestByReferenceIDAndBusinessName() throws Exception {
        PaymentRequest request = ObjectGenerator.generate(PaymentRequest.class);
        assertEquals(paymentRequestDAO.save(request), 1);
    }

    @Test
    public void saveOrUpdatePaymentRequest() throws Exception {
        PaymentRequest request = ObjectGenerator.generate(PaymentRequest.class);
        assertEquals(paymentRequestDAO.save(request), 1);
        request.setStatus(100);
        long now = System.currentTimeMillis() + 100000;
        request.setLastUpdateTime(new Timestamp(now));
        assertEquals(paymentRequestDAO.update(request), 1);
        PaymentRequest result = paymentRequestDAO.getPaymentRequestByID(request.getId());
        assertNotNull(result);
        assertEquals(result.getStatus(), 100);
        assertEquals(result.getLastUpdateTime().getTime(), now);
        assertEquals(result.getBusiness(), request.getBusiness());
        assertEquals(result.getCallBackMetaInfo(), request.getCallBackMetaInfo());
        assertEquals(result.getChannel(), request.getChannel());
        assertEquals(result.getCreateTime(), request.getCreateTime());
        assertEquals(result.getCustomerID(), request.getCustomerID());
        assertEquals(result.getPaymentOperationType(), request.getPaymentOperationType());
        assertEquals(result.getRequestedAmount(), request.getRequestedAmount());
        
    }
}
