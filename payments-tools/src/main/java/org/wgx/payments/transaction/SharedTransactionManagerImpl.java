package org.wgx.payments.transaction;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import javax.sql.DataSource;

import org.wgx.payments.exception.DAOFrameWorkException;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

/**
 * Shared implementation of {@linkplain TransactionManager}.
 * Developers use shared data bases to speed up their application's performance,
 * to make it easier to solve the problems like node selection, we provide this
 * transaction implementation.
 *
 * XA transaction or anything like that will not be supported, once db node is selected all the
 * actions will be executed in that single db node, which means developers should design their tables
 * cautious.
 * @author weiguanxiong
 *
 */
@Slf4j
public class SharedTransactionManagerImpl extends org.apache.tomcat.jdbc.pool.DataSource implements TransactionManager {

    @Setter
    private DBNodeSelector dbNodeSelector;

    @Setter
    private List<DataSource> dataSources;

    @Setter
    private DataSource idAllocatorSource;

    private volatile ThreadLocal<Boolean> isAutoCommit = new ThreadLocal<>();

    private ThreadLocal<Connection> connection = new ThreadLocal<>();

    /**
     * {@inheritDoc}
     */
    @Override
    public Connection getConnection() throws SQLException {
        if (connection.get() == null) {
            /**
             * Delegate the action to the underlying data source, here we used tomcat-jdbc to complete the real job.
             */
            DataSource ds = dbNodeSelector.select(dataSources);
            Connection dbConnection = ds.getConnection();
            dbConnection.setAutoCommit(getAutoCommit());
            connection.set(dbConnection);
        }
        return connection.get();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void releaseConnection() {
        isAutoCommit.set(true);
        Connection dbConnection = connection.get();
        if (dbConnection != null) {
            try {
                dbConnection.close();
            } catch (SQLException e) {
                log.info("Fail to release db connection", e);
            }
            connection.set(null);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setConnection(final Connection dbConnection) {
        connection.set(dbConnection);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean getAutoCommit() {
        return isAutoCommit.get();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setAutoCommit(final boolean autoCommit) {
        isAutoCommit.set(true);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void commit() {
        if (connection.get() == null) {
            throw new DAOFrameWorkException("Connection not exists");
        }
        try {
            connection.get().commit();
        } catch (SQLException e) {
            throw new DAOFrameWorkException("Fail to commit operations", e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void rollback() {
        if (connection.get() == null) {
            throw new DAOFrameWorkException("Connection not exists");
        }
        try {
            connection.get().rollback();
        } catch (SQLException e) {
            throw new DAOFrameWorkException("Fail to commit operations", e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long allocateID(final String table) {
        return IDAllocator.allocateID(table, idAllocatorSource);
    }

}
