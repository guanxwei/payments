package org.wgx.payments.dao;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.sql.Connection;

import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.support.SqlSessionDaoSupport;
import org.wgx.payments.transaction.TransactionManager;

import lombok.Setter;

public abstract class BaseFrameWorkDao<T> extends SqlSessionDaoSupport {

    private SqlSessionFactory sqlSessionFactory;

    @Setter
    private Class<T> daoInterface;

    protected long allocatedID(final String table) {
        return transactionManager.allocateID(table);
    }

    @SuppressWarnings("unchecked")
    public BaseFrameWorkDao() {
        super();
        Type type = getClass().getGenericSuperclass();
        if (type instanceof ParameterizedType) {
            ParameterizedType parameterizedType = (ParameterizedType) type;
            Type actualtype = parameterizedType.getActualTypeArguments()[0];
            String className = actualtype.getTypeName();
            try {
                this.daoInterface = (Class<T>) Class.forName(className);
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Setter
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

    protected T getMapper() {
        return getSession().getMapper(daoInterface);
    }

    protected void close() {
        if (transactionManager.getAutoCommit()) {
            transactionManager.releaseConnection();
        }
    }

}
