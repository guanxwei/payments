package org.wgx.payments.dao;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;
import org.springframework.context.annotation.PropertySource;
import org.wgx.payments.dao.impl.ActionRecordDAOImpl;
import org.wgx.payments.dao.impl.CheckOrderDiffDAOImpl;
import org.wgx.payments.dao.impl.CheckbookItemDAOImpl;
import org.wgx.payments.dao.impl.FastSearchTableDAOImpl;
import org.wgx.payments.dao.impl.PaymentRequestDAOImpl;
import org.wgx.payments.dao.impl.PaymentResponseDAOImpl;
import org.wgx.payments.dao.impl.PaymentStatisticDAOImpl;
import org.wgx.payments.dao.impl.ScheduleJobRecordDAOImpl;
import org.wgx.payments.transaction.TransactionAspect;

@ImportResource(locations = {"classpath:spring-mybatis.xml"})
@PropertySource({"classpath:jdbc.properties"})
@Configuration
public class DAOConfiguration {

    @Bean
    public TransactionAspect transactionAspect() {
        TransactionAspect aspect = new TransactionAspect();
        return aspect;
    }

    @Bean
    public ActionRecordDAO actionRecordDAO() {
        ActionRecordDAOImpl bean = new ActionRecordDAOImpl();
        return bean;
    }

    @Bean
    public CheckbookItemDAO checkbookItemDAO() {
        CheckbookItemDAOImpl bean = new CheckbookItemDAOImpl();
        return bean;
    }

    @Bean
    public CheckOrderDiffDAO checkOrderDiffDAO() {
        CheckOrderDiffDAOImpl bean = new CheckOrderDiffDAOImpl();
        return bean;
    }

    @Bean
    public FastSearchTableDAO fastSearchTableDAO() {
        FastSearchTableDAOImpl bean = new FastSearchTableDAOImpl();
        return bean;
    }

    @Bean
    public PaymentRequestDAO paymentRequestDAO() {
        PaymentRequestDAOImpl bean = new PaymentRequestDAOImpl();
        return bean;
    }

    @Bean
    public PaymentResponseDAO paymentResponseDAO() {
        PaymentResponseDAOImpl bean = new PaymentResponseDAOImpl();
        return bean;
    }

    @Bean
    public PaymentStatisticDAO paymentStatisticDAO() {
        PaymentStatisticDAOImpl bean = new PaymentStatisticDAOImpl();
        return bean;
    }

    @Bean
    public ScheduleJobRecordDAO scheduleJobRecordDAO() {
        ScheduleJobRecordDAOImpl bean = new ScheduleJobRecordDAOImpl();
        return bean;
    }
}
