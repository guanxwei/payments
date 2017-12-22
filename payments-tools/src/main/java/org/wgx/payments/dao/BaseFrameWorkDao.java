package org.wgx.payments.dao;

import java.sql.Connection;
import java.util.function.Supplier;

import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.support.SqlSessionDaoSupport;
import org.springframework.beans.factory.annotation.Autowired;
import org.wgx.payments.transaction.TransactionManager;

import lombok.Setter;

public abstract class BaseFrameWorkDao extends SqlSessionDaoSupport {

    private SqlSessionFactory sqlSessionFactory;
    @Setter @Autowired
    private TransactionManager transactionManager;

    public void setSqlSessionFactory(final SqlSessionFactory sqlSessionFactory) {
        super.setSqlSessionFactory(sqlSessionFactory);
        this.sqlSessionFactory = sqlSessionFactory;
    }

    protected Connection getConnection() {  
        return getSqlSession().getConnection();  
    }

    protected SqlSession getSession() {
        return sqlSessionFactory.openSession();
    }

    protected <T> T process(Supplier<T> supplier) {
        try {
            return supplier.get();
        } catch (Exception e) {
            throw e;
        } finally {
            close();
        }
    }

    protected <T> T getMapper(final Class<T> daoInterface) {
        return getSession().getMapper(daoInterface);
    }

    protected void close() {
        if (transactionManager.getAutoCommit()) {
            transactionManager.releaseConnection();
        }
    }
}
