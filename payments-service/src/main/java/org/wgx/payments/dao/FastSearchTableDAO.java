package org.wgx.payments.dao;

import java.util.List;

import org.wgx.payments.model.FastSearchTableItem;

/**
 * FastSearchTableDAO.
 *
 */
public interface FastSearchTableDAO {

    /**
     * Save an item in table.
     * @param item Item to be saved.
     * @return DB manipulation.
     */
    int save(final FastSearchTableItem item);

    /**
     * Find a item via key.
     * @param key Item key.
     * @return Item.
     */
    FastSearchTableItem find(final String key);

    /**
     * Find fast search table items via key and status.
     * @param key Key to be searched.
     * @param status Target status.
     * @return Items fulfills the requirement.
     */
    List<FastSearchTableItem> list(final String key, final int status);

    /**
     * Try to update the item's status from initiateStatus to status.
     * @param initiateStatus Initiate status.
     * @param status Target status.
     * @param id Item's id.
     * @return Manipulation result.
     */
    boolean tryUpdateStatus(final int initiateStatus, final int status, final long id);

    /**
     * Delete item.
     * @param id Item's id who's to be deleted.
     * @return DB manipulation.
     */
    boolean deleteItem(final long id);

    /**
     * Delete an item via its key.
     * @param key Item's key.
     * @return Manipulation result.
     */
    boolean deleteItemByKey(final String key);

    /**
     * Find fast search item list by status.
     * @param status Item status.
     * @return FastSearchTableItem list.
     */
    List<FastSearchTableItem> findItemsByStatus(final int status);
}
