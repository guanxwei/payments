package org.wgx.payments.transaction;

import java.sql.Connection;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.wgx.payments.exception.DAOFrameWorkException;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TransactionManagerImpl extends org.apache.tomcat.jdbc.pool.DataSource implements TransactionManager {

    @Setter
    private DataSource dataSource;
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
            Connection dbConnection = dataSource.getConnection();
            dbConnection.setAutoCommit(getAutoCommit());
            connection.set(dbConnection);
        }
        return connection.get();
    }

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

    @Override
    public void setConnection(Connection dbConnection) {
        connection.set(dbConnection);
    }

    @Override
    public boolean getAutoCommit() {
        if (isAutoCommit.get() == null) {
            return true;
        }
        return isAutoCommit.get();
    }

    @Override
    public void setAutoCommit(boolean autoCommit) {
        isAutoCommit.set(autoCommit);
    }

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

}
