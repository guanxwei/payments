package org.wgx.payments.dao.impl;

import java.util.List;

import org.wgx.payments.dao.BaseFrameWorkDao;
import org.wgx.payments.dao.CheckbookItemDAO;
import org.wgx.payments.dao.DAOMethod;
import org.wgx.payments.dao.TableMapping;
import org.wgx.payments.model.CheckbookItem;

/**
 * Mybatis based implementation of {@linkplain CheckbookItemDAO}.
 *
 */
@TableMapping(table = "CheckbookItem")
public class CheckbookItemDAOImpl extends BaseFrameWorkDao<CheckbookItemDAO> implements CheckbookItemDAO {

    /**
     * {@inheritDoc}
     */
    @DAOMethod
    @Override
    public List<CheckbookItem> getListByTransactionID(final String transactionID) {
        return getMapper().getListByTransactionID(transactionID);
    }

    /**
     * {@inheritDoc}
     */
    @DAOMethod
    @Override
    public List<CheckbookItem> list(final int limit, final int offset, final int status) {
        return getMapper().list(limit, offset, status);
    }

    /**
     * {@inheritDoc}
     */
    @DAOMethod
    @Override
    public int save(final CheckbookItem item) {
        return getMapper().save(item);
    }

    /**
     * {@inheritDoc}
     */
    @DAOMethod
    @Override
    public CheckbookItem findByUniqueItem(final String uniqueKey) {
        return getMapper().findByUniqueItem(uniqueKey);
    }

    /**
     * {@inheritDoc}
     */
    @DAOMethod
    @Override
    public List<CheckbookItem> getCheckbookItemsByRange(final String beginTime, final String endTime) {
        return getMapper().getCheckbookItemsByRange(beginTime, endTime);
    }

}
