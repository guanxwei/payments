package org.wgx.payments.dao;

import java.lang.annotation.Annotation;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.sql.Connection;

import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.wgx.payments.transaction.TransactionManager;

import lombok.Getter;
import lombok.Setter;

public abstract class BaseFrameWorkDao<T> {

    private SqlSessionFactory sqlSessionFactory;

    @Setter
    private Class<T> daoInterface;

    @Getter
    private String table;
    @Getter
    private String id;
    @Getter
    private String insertMethod;

    protected long allocatedID() {
        return transactionManager.allocateID(table);
    }

    public BaseFrameWorkDao() {
        setDaoInterface();
        setTableInfo();
    }

    @SuppressWarnings("unchecked")
    private void setDaoInterface() {
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

    private void setTableInfo() {
        Annotation[] annotations = this.getClass().getAnnotations();
        if (annotations != null && annotations.length > 0) {
            for (Annotation annotation : annotations) {
                if (annotation instanceof TableMapping) {
                    TableMapping mapping = TableMapping.class.cast(annotation);
                    insertMethod = mapping.insertMethod();
                    id = mapping.id();
                    table = mapping.table();
                }
            }
        }
    }

    @Setter
    private TransactionManager transactionManager;

    public void setSqlSessionFactory(final SqlSessionFactory sqlSessionFactory) {
        this.sqlSessionFactory = sqlSessionFactory;
    }

    protected Connection getConnection() {  
        return sqlSessionFactory.openSession().getConnection();  
    }

    protected SqlSession getSession() {
        return sqlSessionFactory.openSession();
    }

    protected T getMapper() {
        return sqlSessionFactory.openSession().getMapper(daoInterface);
    }

    protected void close() {
        if (transactionManager.getAutoCommit()) {
            transactionManager.releaseConnection();
        }
    }

}
