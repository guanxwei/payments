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
import org.wgx.payments.deducer.WechatTradeTypeDeducer;
import org.wgx.payments.facade.Facade;
import org.wgx.payments.processors.AccountsProcessor;
import org.wgx.payments.processors.AlipayProcessor;
import org.wgx.payments.processors.IAPProcessor;
import org.wgx.payments.processors.PointsProcessor;
import org.wgx.payments.processors.WechatProcessor;
import org.wgx.payments.signature.AccountFactory;
import org.wgx.payments.signature.SignatureGenerator;
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
     * PaymentProcessorManager bean definition.
     * @return PaymentProcessorManager instance.
     */
    @Bean(name = "paymentProcessorManager")
    public PaymentProcessorManager paymentProcessorManager() {
        PaymentProcessorManager paymentProcessorManager = new PaymentProcessorManager();
        paymentProcessorManager.registerProcessor(appleIAPProcessor());
        paymentProcessorManager.registerProcessor(alipayProecssor());
        paymentProcessorManager.registerProcessor(wechatProecssor());
        paymentProcessorManager.registerProcessor(pointsProcessor());
        paymentProcessorManager.registerProcessor(accountsProcessor());
        return paymentProcessorManager;
    }

    /**
     * IAP payment processor.
     * @return IAPProcessor.
     */
    @Bean(name = "IAPProecssor")
    public PaymentProcessor appleIAPProcessor() {
        return new IAPProcessor();
    }

    /**
     * Alipay payment processor.
     * @return AlipayProcessor.
     */
    @Bean(name = "alipayProecssor")
    public PaymentProcessor alipayProecssor() {
        AlipayProcessor processor = new AlipayProcessor();
        processor.setCallback(callback);
        processor.setPaymentRequestDAO(paymentRequestDAO);
        processor.setPaymentResponseDAO(paymentResponseDAO);
        processor.setPaymentsDAOService(paymentsDAOService);
        processor.setSignatureGenerator(SignatureGenerator.BASE_32);
        processor.setValidator(alipayValidator);
        processor.setActionRecordDAO(actionRecordDAO);
        processor.setFacade(alipayFacade);
        processor.setBusinessProfileDeducer(businessProfileDeducer());
        processor.setFastSearchTableDAO(fastSearchTableDAO);
        processor.setAccountFactory(accountFactory);
        processor.setAccountDeducer(accountDeducer());
        return processor;
    }

    /**
     * Wechat payment processor.
     * @return WechatProcessor.
     */
    @Bean(name = "wechatProecssor")
    public PaymentProcessor wechatProecssor() {
        WechatProcessor processor = new WechatProcessor();
        processor.setCallback(callback);
        processor.setPaymentRequestDAO(paymentRequestDAO);
        processor.setPaymentResponseDAO(paymentResponseDAO);
        processor.setPaymentsDAOService(paymentsDAOService);
        processor.setSignatureGenerator(SignatureGenerator.BASE_32);
        processor.setActionRecordDAO(actionRecordDAO);
        processor.setValidator(wechatValidator);
        processor.setFacade(wechatFacade);
        processor.setBusinessProfileDeducer(businessProfileDeducer());
        processor.setFastSearchTableDAO(fastSearchTableDAO);
        processor.setAccountFactory(accountFactory);
        processor.setTradeTypeDeducer(new WechatTradeTypeDeducer());
        processor.setAccountDeducer(accountDeducer());

        return processor;
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

    /**
     * Points payment processor.
     * @return PointsProcessor.
     */
    @Bean(name = "pointsProcessor")
    public PaymentProcessor pointsProcessor() {
        PointsProcessor processor = new PointsProcessor();
        return processor;
    }

    /**
     * Account payment processor.
     * @return AccountsProcessor.
     */
    @Bean(name = "accountsProcessor")
    public PaymentProcessor accountsProcessor() {
        AccountsProcessor processor = new AccountsProcessor();
        return processor;
    }
}
