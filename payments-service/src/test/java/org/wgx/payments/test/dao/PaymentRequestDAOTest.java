package org.wgx.payments.test.dao;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

import java.sql.Timestamp;
import java.util.List;

import javax.annotation.Resource;

import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.Test;
import org.wgx.payments.dao.DAOConfiguration;
import org.wgx.payments.dao.PaymentRequestDAO;
import org.wgx.payments.model.PaymentRequest;
import org.wgx.payments.tools.ObjectGenerator;

@ContextConfiguration(classes = {
        DAOConfiguration.class,
        DaoTestConfiguration.class
    })
public class PaymentRequestDAOTest extends AbstractTestNGSpringContextTests {

    @Resource
    private PaymentRequestDAO paymentRequestDAO;

    @Test
    public void testgetPaymentRequestByReferenceIDAndBusinessName() throws Exception {
        PaymentRequest request = ObjectGenerator.generate(PaymentRequest.class);
        assertEquals(paymentRequestDAO.save(request), 1);
        assertNotNull(paymentRequestDAO.getPaymentRequestByReferenceIDAndBusinessName(request.getReferenceID(), request.getBusiness()));
    }

    @Test
    public void saveOrUpdatePaymentRequest() throws Exception {
        PaymentRequest request = ObjectGenerator.generate(PaymentRequest.class);
        assertEquals(paymentRequestDAO.save(request), 1);
        request.setStatus(100);
        long now = System.currentTimeMillis() + 100000;
        request.setLastUpdateTime(new Timestamp(now));
        assertEquals(paymentRequestDAO.update(request), 1);
        PaymentRequest result = paymentRequestDAO.getPaymentRequestByTransactionID(request.getTransactionID());
        assertNotNull(result);
        assertEquals(result.getStatus(), 100);
        assertEquals(result.getLastUpdateTime().getTime(), now);
        assertEquals(result.getBusiness(), request.getBusiness());
        assertEquals(result.getCallBackMetaInfo(), request.getCallBackMetaInfo());
        assertEquals(result.getChannel(), request.getChannel());
        assertEquals(result.getCreateTime(), request.getCreateTime());
        assertEquals(result.getCustomerID(), request.getCustomerID());
        assertEquals(result.getParentID(), request.getParentID());
        assertEquals(result.getPaymentMethod(), request.getPaymentMethod());
        assertEquals(result.getPaymentOperationType(), request.getPaymentOperationType());
        assertEquals(request.getReferenceID(), request.getReferenceID());
        assertEquals(result.getRequestedAmount(), request.getRequestedAmount());
        assertEquals(result.getTransactionID(), request.getTransactionID());
        assertEquals(result.getUrl(), request.getUrl());
        
    }

    @Test
    public void getPaymentRequestsByParentID() throws Exception {
        PaymentRequest request = ObjectGenerator.generate(PaymentRequest.class);
        assertEquals(paymentRequestDAO.save(request), 1);
        assertNotNull(paymentRequestDAO.getPaymentRequestsByParentID(request.getParentID()));
    }

    @Test
    public void getPendingPaymentRequestList() throws Exception {
        PaymentRequest request = ObjectGenerator.generate(PaymentRequest.class);
        assertEquals(paymentRequestDAO.save(request), 1);
        List<PaymentRequest> list = paymentRequestDAO.getPendingPaymentRequestList(request.getStatus(), 
                request.getPaymentMethod(), request.getPaymentOperationType(), 20);
        assertNotNull(list);
        assertEquals(list.size(), 1);
        assertEquals(list.get(0).getBusiness(), request.getBusiness());
    }
}
