package org.wgx.payments.test.dao;

import static org.testng.Assert.assertEquals;

import javax.annotation.Resource;

import org.testng.annotations.Test;
import org.wgx.payments.account.impl.meta.PaymentAccountScope;
import org.wgx.payments.dao.PaymentAccountScopeDAO;
import org.wgx.payments.tools.ObjectGenerator;

public class PaymentAccountScopeDAOTest extends DAOTestBase {

    @Resource
    private PaymentAccountScopeDAO paymentAccountScopeDAO;

    @Test
    public void testsave() throws Exception {
        PaymentAccountScope scope = ObjectGenerator.generate(PaymentAccountScope.class);
        assertEquals(paymentAccountScopeDAO.save(scope), 1);
    }

    @Test
    public void testedit() throws Exception {
        PaymentAccountScope scope = ObjectGenerator.generate(PaymentAccountScope.class);
        assertEquals(paymentAccountScopeDAO.save(scope), 1);
        scope.setDeviceType("test");
        paymentAccountScopeDAO.edit(scope);
        PaymentAccountScope result = paymentAccountScopeDAO.find(scope.getAccountID());
        assertEquals(result.getSupportedOperations(), scope.getSupportedOperations());
        assertEquals(result.getDeviceType(), "test");
    }

    @Test
    public void testgetAccountCount() throws Exception {
        PaymentAccountScope scope = ObjectGenerator.generate(PaymentAccountScope.class);
        assertEquals(paymentAccountScopeDAO.save(scope), 1);
        assertEquals(paymentAccountScopeDAO.getAccountCount(scope.getAccountID()), 1);
    }
}
