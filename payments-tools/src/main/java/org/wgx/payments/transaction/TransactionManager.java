package org.wgx.payments.transaction;

import java.sql.Connection;

import javax.sql.DataSource;

public interface TransactionManager extends DataSource {

    /**
     * Release the previous retrieved connection.
     */
    public void releaseConnection();

    /**
     * Reset the connection.
     * @param connection New connection.
     */
    public void setConnection(final Connection connection);

    /**
     * Check if currently the underlying connection is configured as auto committed.
     */
    public boolean getAutoCommit();

    /**
     * Update the auto commit flag of the underlying connection.
     */
    public void setAutoCommit(final boolean autoCommit);

    /**
     * Commit the buffering operations to the remote DB.
     */
    public void commit();

    /**
     * Rollback the operations.
     */
    public void rollback();
}
