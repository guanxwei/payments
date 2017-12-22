package org.wgx.payments.dao;

import java.util.List;

import org.wgx.payments.model.ActionRecord;

/**
 * ActionRecordDAO.
 *
 */
public interface ActionRecordDAO {

    /**
     * Add a record in the DB.
     * @param record Record need to be stored.
     * @return DB manipulation result.
     */
    int record(final ActionRecord record);

    /**
     * Query actions corresponding to the specific transaction id.
     * @param transactionID Target transaction id.
     * @return Action record list corresponding to the transaction.
     */
    List<ActionRecord> queryByTransactionID(final String transactionID);
}
