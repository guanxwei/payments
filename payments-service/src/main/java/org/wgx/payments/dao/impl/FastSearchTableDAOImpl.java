package org.wgx.payments.dao.impl;

import java.util.List;

import org.wgx.payments.dao.BaseFrameWorkDao;
import org.wgx.payments.dao.DAOMethod;
import org.wgx.payments.dao.FastSearchTableDAO;
import org.wgx.payments.dao.TableMapping;
import org.wgx.payments.model.FastSearchTableItem;

/**
 * Mybatis based implementation of {@linkplain FastSearchTableDAO}.
 *
 */
@TableMapping(table = "FastSearchTableItem")
public class FastSearchTableDAOImpl extends BaseFrameWorkDao<FastSearchTableDAO> implements FastSearchTableDAO {

    /**
     * {@inheritDoc}
     */
    @DAOMethod
    @Override
    public int save(final FastSearchTableItem item) {
        return getMapper().save(item);
    }

    /**
     * {@inheritDoc}
     */
    @DAOMethod
    @Override
    public FastSearchTableItem find(final String key) {
        return getMapper().find(key);
    }

    /**
     * {@inheritDoc}
     */
    @DAOMethod
    @Override
    public List<FastSearchTableItem> list(final String key, final int status) {
        return getMapper().list(key, status);
    }

    /**
     * {@inheritDoc}
     */
    @DAOMethod
    @Override
    public boolean tryUpdateStatus(final int initiateStatus, final int status, final long id) {
        return getMapper().tryUpdateStatus(initiateStatus, status, id);
    }

    /**
     * {@inheritDoc}
     */
    @DAOMethod
    @Override
    public boolean deleteItem(final long id) {
        return getMapper().deleteItem(id);
    }

    /**
     * {@inheritDoc}
     */
    @DAOMethod
    @Override
    public boolean deleteItemByKey(final String key) {
        return getMapper().deleteItemByKey(key);
    }

    /**
     * {@inheritDoc}
     */
    @DAOMethod
    @Override
    public List<FastSearchTableItem> findItemsByStatus(final int status) {
        return getMapper().findItemsByStatus(status);
    }

}
