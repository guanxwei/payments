package org.wgx.payments.transaction;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import javax.sql.DataSource;

import org.wgx.payments.exception.DAOFrameWorkException;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TransactionManagerImpl extends org.apache.tomcat.jdbc.pool.DataSource implements TransactionManager {

    private static final long STEP = 1000L;

    /**
    * 分布式ID分配表，配置信息，必须在应用创建时也一并创建本表.
    * 必须包含的字段为id bigint, tableName varchar(1024), cursor bigint, updateTime bigint.
    */
    private static final String ID_TABLE = "PaymentTables";

    private Map<String, TableID> ids = new HashMap<>();

    @Setter
    private DataSource ds;

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
    public void setConnection(Connection dbConnection) {
        connection.set(dbConnection);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean getAutoCommit() {
        if (isAutoCommit.get() == null) {
            return true;
        }
        return isAutoCommit.get();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setAutoCommit(boolean autoCommit) {
        isAutoCommit.set(autoCommit);
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
        TableID tableID = ids.get(table);
        if (tableID == null) {
            tableID = initiate(table);
        }

        long remain = tableID.remaining.decrementAndGet();
        if (remain <= 0) {
            return updateAndReturn(table);
        } else {
            return tableID.cursor.getAndIncrement();
        }
    }

    private TableID initiate(final String table) {
        synchronized (table) {
            try (Connection connection = ds.getConnection();) {
                connection.setAutoCommit(false);
                String sql = "select cursor from " + ID_TABLE + " where tableName = ? for update";
                PreparedStatement preparedStatement = connection.prepareStatement(sql);
                preparedStatement.setString(1, table);
                ResultSet resultSet = preparedStatement.executeQuery();
                long cursor = 0;
                if (!resultSet.next()) {
                    String insert = "insert into " + ID_TABLE + "(id, tableName, cursor, updateTime) values(?, ?, ?, ?)";
                    PreparedStatement insertStatement = connection.prepareStatement(insert);
                    insertStatement.setLong(1, System.currentTimeMillis());
                    insertStatement.setString(2, table);
                    insertStatement.setLong(3, cursor + STEP);
                    insertStatement.setLong(4, System.currentTimeMillis());
                    insertStatement.executeUpdate();
                    connection.commit();
                    preparedStatement.close();
                    insertStatement.close();
                } else {
                    cursor = resultSet.getLong("cursor");
                    String update = "update " + ID_TABLE + " set cursor = ?, updateTime = ? where tableName = ?";
                    PreparedStatement updateStatement = connection.prepareStatement(update);
                    updateStatement.setLong(1, cursor + STEP);
                    updateStatement.setLong(2, System.currentTimeMillis());
                    updateStatement.setString(3, table);
                    updateStatement.executeUpdate();
                    connection.commit();
                    preparedStatement.close();
                    updateStatement.close();
                }

                TableID tableID = new TableID();
                tableID.cursor = new AtomicLong(cursor + STEP);
                tableID.remaining = new AtomicLong(STEP);
                return tableID;
            } catch (SQLException e) {
                throw new RuntimeException(String.format("Fail to allocated id for table [%s]", table), e);
            }
        }
    }

    private long updateAndReturn(final String table) {
        synchronized (table) {
            TableID tableID = initiate(table);
            ids.put(table, tableID);
            tableID.remaining.decrementAndGet();
            return tableID.cursor.getAndIncrement();
        }
    }

    public class TableID {
        private AtomicLong cursor;
        private AtomicLong remaining;
    }
}
