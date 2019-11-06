package org.wgx.payments.transaction;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import javax.sql.DataSource;

import lombok.Data;

public class IDAllocator {

    /**
    * 分布式ID分配表，配置信息，必须在应用创建时也一并创建本表.
    * 必须包含的字段为id bigint, tableName varchar(1024), cursor bigint, updateTime bigint.
    */
    public static final String ID_TABLE = "PaymentTables";

    /**
     * Allocate step
     */
    public static final long STEP = 1000L;

    /**
     * Currently allocated ids for the tables.
     */
    public static final Map<String, TableID> IDS = new HashMap<>();

    public static long allocateID(final String table, final DataSource dataSource) {
        TableID tableID = IDS.get(table);
        if (tableID == null) {
            tableID = initiate(table, dataSource);
            IDS.put(table, tableID);
        }

        long remain = tableID.getRemaining().decrementAndGet();
        if (remain <= 0) {
            return updateAndReturn(table, dataSource);
        } else {
            return tableID.getCursor().getAndIncrement();
        }
    }

    /**
     * Initiate the table is possible.
     * @param table Table to be initiated.
     * @param ds Data source used to store the data.
     * @return tableid allocated.
     */
    private static TableID initiate(final String table, final DataSource ds) {
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

    private static long updateAndReturn(final String table, final DataSource ds) {
        synchronized (table) {
            TableID tableID = initiate(table, ds);
            IDS.put(table, tableID);
            tableID.remaining.decrementAndGet();
            return tableID.cursor.getAndIncrement();
        }
    }

    /**
     * Table id 包装.
     * @author weiguanxiong
     *
     */
    @Data
    public static class TableID {
        private AtomicLong cursor;
        private AtomicLong remaining;
    }
}
