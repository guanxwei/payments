package org.wgx.payments.dao;

import java.util.List;

import org.wgx.payments.model.CheckOrderDiffItem;

/**
 * author hzxuwei3.
 * date 2017年4月20日 下午1:44:11
 */
public interface CheckOrderDiffDAO {
    /**
     * Save the difference when checking.
     * @param checkOrderDiffItem Difference item
     * @return DB manipulation result.
     */
    int save(final CheckOrderDiffItem checkOrderDiffItem);

    /**
     * Find CheckOrderDiffItem by id.
     * @param id CheckOrderDiffItem id.
     * @return CheckOrderDiffItem.
     */
    CheckOrderDiffItem find(final long id);

    /**
     * Get checkbook issue item list from the DB based on the input query condition.
     * @param limit Item numeric limit.
     * @param offset The offset of the first item.
     * @param status Item status.
     * @return Checkbook item list.
     */
    List<CheckOrderDiffItem> list(final int limit, final int offset, final int status);

    /**
     * Update checkbook issue item status by item id.
     * @param id Item id.
     * @param status Item status.
     * @return DB manipulation.
     */
    int updateStatus(final long id, final int status);

    /**
     * Get check book diff item's quantity by status.
     * @param status Items' status.
     * @return Count.
     */
    int count(final int status);
}
