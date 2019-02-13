package org.wgx.payments.transaction;

import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.logging.Logger;

import lombok.Setter;

/**
 * Shared implementation of {@linkplain TransactionManager}.
 * Developers use shared data bases to speed up their application's performance,
 * to make it easier to solve the problems like node selection, we provide this
 * transaction implementation.
 *
 * XA transaction or anything like that will be not supported, once db node is selected all the
 * actions will be executed in that single db node, which means developers should design their tables
 * cautious.
 * @author weiguanxiong
 *
 */
public class SharedTransactionManagerImpl implements TransactionManager {

    @Setter
    private DBNodeSelector dbNodeSelector;

    /**
     * {@inheritDoc}
     */
    @Override
    public Connection getConnection() throws SQLException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Connection getConnection(String username, String password) throws SQLException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public PrintWriter getLogWriter() throws SQLException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void setLogWriter(PrintWriter out) throws SQLException {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void setLoginTimeout(int seconds) throws SQLException {
        // TODO Auto-generated method stub
        
    }

    @Override
    public int getLoginTimeout() throws SQLException {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public Logger getParentLogger() throws SQLFeatureNotSupportedException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public <T> T unwrap(Class<T> iface) throws SQLException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean isWrapperFor(Class<?> iface) throws SQLException {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public void releaseConnection() {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void setConnection(Connection connection) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public boolean getAutoCommit() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public void setAutoCommit(boolean autoCommit) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void commit() {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void rollback() {
        // TODO Auto-generated method stub
        
    }

    @Override
    public long allocateID(String table) {
        // TODO Auto-generated method stub
        return 0;
    }

}
