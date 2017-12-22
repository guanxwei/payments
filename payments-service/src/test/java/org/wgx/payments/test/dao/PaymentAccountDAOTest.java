package org.wgx.payments.test.dao;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

import java.util.List;

import javax.annotation.Resource;

import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.Test;
import org.wgx.payments.account.impl.meta.PaymentAccount;
import org.wgx.payments.dao.DAOConfiguration;
import org.wgx.payments.dao.PaymentAccountDAO;
import org.wgx.payments.tools.ObjectGenerator;

@ContextConfiguration(classes = {
        DAOConfiguration.class,
        DaoTestConfiguration.class
    })
public class PaymentAccountDAOTest extends AbstractTestNGSpringContextTests {

    @Resource
    private PaymentAccountDAO paymentAccountDAO;

    @Test
    public void testsave() throws Exception {
        PaymentAccount account = ObjectGenerator.generate(PaymentAccount.class);
        assertEquals(paymentAccountDAO.save(account), 1);
    }

    @Test
    public void testedit() throws Exception {
        PaymentAccount account = ObjectGenerator.generate(PaymentAccount.class);
        assertEquals(paymentAccountDAO.save(account), 1);
        account.setPaymentMethod("test");
        assertEquals(paymentAccountDAO.edit(account), 1);
        PaymentAccount result = paymentAccountDAO.get(account.getId());
        assertEquals(account.getPaymentMethod(), result.getPaymentMethod());
        assertEquals("test", result.getPaymentMethod());

    }

    @Test
    public void testgetAccountNameCount() throws Exception {
        PaymentAccount account = ObjectGenerator.generate(PaymentAccount.class);
        assertEquals(paymentAccountDAO.save(account), 1);
        assertEquals(paymentAccountDAO.getAccountNameCount(account.getAccountName()), 1);
    }

    @Test
    public void testquery() throws Exception {
        PaymentAccount account = ObjectGenerator.generate(PaymentAccount.class);
        assertEquals(paymentAccountDAO.save(account), 1);
        List<PaymentAccount> accounts = paymentAccountDAO.query(account.getAccountName());
        assertNotNull(accounts);
        assertEquals(accounts.size(), 1);
        assertEquals(accounts.get(0).getAccountNo(), account.getAccountNo());
    }

    @Test
    public void testgetByPaymentMethod() throws Exception {
        PaymentAccount account = ObjectGenerator.generate(PaymentAccount.class);
        assertEquals(paymentAccountDAO.save(account), 1);
        List<PaymentAccount> accounts = paymentAccountDAO.getByPaymentMethod(account.getPaymentMethod());
        assertNotNull(accounts);
        assertEquals(accounts.size(), 1);
        assertEquals(accounts.get(0).getAccountNo(), account.getAccountNo());
    }
}
