package org.wgx.payments.dao;

import javax.annotation.Resource;

import org.apache.ibatis.session.SqlSessionFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;
import org.springframework.context.annotation.PropertySource;
import org.wgx.payments.dao.impl.ActionRecordDAOImpl;
import org.wgx.payments.dao.impl.CheckbookItemDAOImpl;
import org.wgx.payments.dao.impl.FastSearchTableDAOImpl;
import org.wgx.payments.dao.impl.PaymentAccountDAOImpl;
import org.wgx.payments.dao.impl.PaymentAccountScopeDAOImpl;
import org.wgx.payments.dao.impl.PaymentRequestDAOImpl;
import org.wgx.payments.dao.impl.PaymentResponseDAOImpl;
import org.wgx.payments.dao.impl.PaymentStatisticDAOImpl;
import org.wgx.payments.dao.impl.ScheduleJobRecordDAOImpl;
import org.wgx.payments.dao.impl.CheckOrderDiffDAOImpl;
import org.wgx.payments.transaction.TransactionAspect;
import org.wgx.payments.transaction.TransactionManager;

@ImportResource(locations = {"classpath:spring-mybatis.xml"})
@PropertySource({"classpath:jdbc.properties"})
@Configuration
public class DAOConfiguration {

    @Resource
    private TransactionManager transactionManager;

    @Resource
    private SqlSessionFactory sqlSessionFactory;

    @Bean
    public TransactionAspect transactionAspect() {
        TransactionAspect aspect = new TransactionAspect();
        aspect.setTransactionManager(transactionManager);
        return aspect;
    }

    @Bean
    public ActionRecordDAO actionRecordDAO() {
        ActionRecordDAOImpl bean = new ActionRecordDAOImpl();
        bean.setSqlSessionFactory(sqlSessionFactory);
        bean.setTransactionManager(transactionManager);
        return bean;
    }

    @Bean
    public CheckbookItemDAO checkbookItemDAO() {
        CheckbookItemDAOImpl bean = new CheckbookItemDAOImpl();
        bean.setSqlSessionFactory(sqlSessionFactory);
        bean.setTransactionManager(transactionManager);
        return bean;
    }

    @Bean
    public CheckOrderDiffDAO checkOrderDiffDAO() {
        CheckOrderDiffDAOImpl bean = new CheckOrderDiffDAOImpl();
        bean.setSqlSessionFactory(sqlSessionFactory);
        bean.setTransactionManager(transactionManager);
        return bean;
    }

    @Bean
    public FastSearchTableDAO fastSearchTableDAO() {
        FastSearchTableDAOImpl bean = new FastSearchTableDAOImpl();
        bean.setSqlSessionFactory(sqlSessionFactory);
        bean.setTransactionManager(transactionManager);
        return bean;
    }

    @Bean
    public PaymentAccountDAO paymentAccountDAO() {
        PaymentAccountDAOImpl bean = new PaymentAccountDAOImpl();
        bean.setSqlSessionFactory(sqlSessionFactory);
        bean.setTransactionManager(transactionManager);
        return bean;
    }

    @Bean
    public PaymentAccountScopeDAO paymentAccountScopeDAO() {
        PaymentAccountScopeDAOImpl bean = new PaymentAccountScopeDAOImpl();
        bean.setSqlSessionFactory(sqlSessionFactory);
        bean.setTransactionManager(transactionManager);
        return bean;
    }

    @Bean
    public PaymentRequestDAO paymentRequestDAO() {
        PaymentRequestDAOImpl bean = new PaymentRequestDAOImpl();
        bean.setSqlSessionFactory(sqlSessionFactory);
        bean.setTransactionManager(transactionManager);
        return bean;
    }

    @Bean
    public PaymentResponseDAO paymentResponseDAO() {
        PaymentResponseDAOImpl bean = new PaymentResponseDAOImpl();
        bean.setTransactionManager(transactionManager);
        bean.setSqlSessionFactory(sqlSessionFactory);
        return bean;
    }

    @Bean
    public PaymentStatisticDAO paymentStatisticDAO() {
        PaymentStatisticDAOImpl bean = new PaymentStatisticDAOImpl();
        bean.setSqlSessionFactory(sqlSessionFactory);
        bean.setTransactionManager(transactionManager);
        return bean;
    }

    @Bean
    public ScheduleJobRecordDAO scheduleJobRecordDAO() {
        ScheduleJobRecordDAOImpl bean = new ScheduleJobRecordDAOImpl();
        bean.setSqlSessionFactory(sqlSessionFactory);
        bean.setTransactionManager(transactionManager);
        return bean;
    }
}
