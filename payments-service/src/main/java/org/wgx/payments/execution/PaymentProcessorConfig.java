package org.wgx.payments.execution;

import javax.annotation.Resource;

import org.apache.commons.lang3.tuple.Pair;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.wgx.payments.account.PaymentAccountClient;
import org.wgx.payments.callback.Callback;
import org.wgx.payments.client.api.helper.BusinessProfile;
import org.wgx.payments.client.api.io.CreateOrUpdatePaymentResponseRequest;
import org.wgx.payments.client.api.io.Request;
import org.wgx.payments.dao.ActionRecordDAO;
import org.wgx.payments.dao.FastSearchTableDAO;
import org.wgx.payments.dao.PaymentRequestDAO;
import org.wgx.payments.dao.PaymentResponseDAO;
import org.wgx.payments.dao.service.PaymentsDAOService;
import org.wgx.payments.deducer.AccountDeducer;
import org.wgx.payments.deducer.BusinessProfileDeducer;
import org.wgx.payments.deducer.Deducer;
import org.wgx.payments.facade.Facade;
import org.wgx.payments.signature.AccountFactory;
import org.wgx.payments.validator.Validator;

/**
 * Configuration class for payment processors.
 *
 */
@Configuration
public class PaymentProcessorConfig {

    @Resource
    private Validator<CreateOrUpdatePaymentResponseRequest> alipayValidator;

    @Resource
    private Validator<CreateOrUpdatePaymentResponseRequest> wechatValidator;

    @Resource
    private PaymentRequestDAO paymentRequestDAO;

    @Resource
    private PaymentResponseDAO paymentResponseDAO;

    @Resource
    private ActionRecordDAO actionRecordDAO;

    @Resource(name = "callback")
    private Callback callback;

    @Resource(name = "accountFactory")
    private AccountFactory accountFactory;

    @Resource
    private PaymentsDAOService paymentsDAOService;

    @Resource(name = "wechatFacade")
    private Facade<Pair<String, String>, String> wechatFacade;

    @Resource(name = "alipayFacade")
    private Facade<Pair<String, String>, String> alipayFacade;

    @Resource(name = "fastSearchTableDAO")
    private FastSearchTableDAO fastSearchTableDAO;

    @Resource(name = "paymentAccountClient")
    private PaymentAccountClient paymentAccountClient;

    /**
     * Business profile deducer bean definition.
     * @return BusinessProfileDeducer.
     */
    @Bean(name = "businessProfileDeducer")
    public Deducer<String, BusinessProfile> businessProfileDeducer() {
        return new BusinessProfileDeducer();
    }

    /**
     * Account deducer.
     * @return Account deducer.
     */
    @Bean(name = "accountDeducer")
    public Deducer<Pair<Request, String>, Pair<String, String>> accountDeducer() {
        AccountDeducer deducer = new AccountDeducer();
        deducer.setPaymentAccountClient(paymentAccountClient);
        return deducer;
    }

}
