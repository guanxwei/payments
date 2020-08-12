package org.wgx.payments.test.dao;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

import java.util.List;

import javax.annotation.Resource;

import org.testng.annotations.Test;
import org.wgx.payments.dao.PaymentStatisticDAO;
import org.wgx.payments.model.PaymentStatistic;
import org.wgx.payments.tools.ObjectGenerator;

public class PaymentStatisticDAOTest extends DAOTestBase {

    @Resource
    private PaymentStatisticDAO paymentStatisticDAO;

    @Test
    public void save() throws Exception {
        PaymentStatistic item = ObjectGenerator.generate(PaymentStatistic.class);
        assertEquals(paymentStatisticDAO.save(item), 1);
    }

    @Test
    public void getLatestByBusiness() throws Exception {
        PaymentStatistic item = ObjectGenerator.generate(PaymentStatistic.class);
        assertEquals(paymentStatisticDAO.save(item), 1);
        List<PaymentStatistic> result = paymentStatisticDAO.getLatestByBusiness(item.getBusiness(), item.getDate());
        assertNotNull(result);
        assertEquals(result.size(), 1);
    }

    @Test
    public void getLatestByBusinessAndPaymentMethod() throws Exception {
        PaymentStatistic item = ObjectGenerator.generate(PaymentStatistic.class);
        assertEquals(paymentStatisticDAO.save(item), 1);
        List<PaymentStatistic> result = paymentStatisticDAO.getLatestByBusinessAndPaymentMethod(item.getBusiness(),
                item.getPaymentMethod(), item.getDate());
        assertNotNull(result);
        assertEquals(result.size(), 1);
    }

    @Test
    public void getByDate() throws Exception {
        PaymentStatistic item = ObjectGenerator.generate(PaymentStatistic.class);
        assertEquals(paymentStatisticDAO.save(item), 1);
        List<PaymentStatistic> result = paymentStatisticDAO.getByDate(item.getDate() - 1, item.getDate() + 1);
        assertNotNull(result);
        assertEquals(result.size(), 1);
    }
}
