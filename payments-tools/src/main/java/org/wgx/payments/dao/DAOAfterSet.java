package org.wgx.payments.dao;

import javax.annotation.Resource;

import org.apache.ibatis.session.SqlSessionFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.wgx.payments.transaction.TransactionManager;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DAOAfterSet implements BeanPostProcessor {

    @Resource
    private TransactionManager transactionManager;

    @Resource
    private SqlSessionFactory sqlSessionFactory;

    /**
     * {@inheritDoc}
     */
    @Override
    public Object postProcessBeforeInitialization(final Object bean, final String beanName) throws BeansException {
        if (bean instanceof BaseFrameWorkDao<?>) {
            BaseFrameWorkDao<?> dao = (BaseFrameWorkDao<?>) bean;
            dao.setSqlSessionFactory(sqlSessionFactory);
            dao.setTransactionManager(transactionManager);
            log.info("Set sqlsession factory and transaction manager for dao [{}]", dao.getClass().getSimpleName());
        }
        return bean;
    }
}
