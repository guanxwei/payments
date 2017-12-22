package org.wgx.payments.dao;

import java.util.List;

import org.wgx.payments.model.CheckbookItem;

/**
 * Checkbook item DAO.
 */
public interface CheckbookItemDAO {

    /**
     * Get payment checkbook item list by payment transaction id.
     * @param transactionID payment transaction id.
     * @return Checkbook item list.
     */
    List<CheckbookItem> getListByTransactionID(final String transactionID);

    /**
     * Get checkbook item list from the DB based on the input query condition.
     * @param limit Item numeric limit.
     * @param offset The offset of the first item.
     * @param status Item status.
     * @return Checkbook item list.
     */
    List<CheckbookItem> list(final int limit, final int offset, final int status);

    /**
     * Save an item in DB.
     * @param item Item to be saved.
     * @return DB manipulation.
     */
    int save(final CheckbookItem item);

    /**
     * Find an checkbook item via uniqueKey.
     * @param uniqueKey uniqueKey to be searched.
     * @return Checkbook item with uniqueKey of @param uniqueKey
     */
    CheckbookItem findByUniqueItem(final String uniqueKey);

    /**
     * Get check book item list by time range.
     * @param beginTime Begin of the range.
     * @param endTime End of the range.
     * @return Checkbook item list.
     */
    List<CheckbookItem> getCheckbookItemsByRange(final String beginTime, final String endTime);
}
