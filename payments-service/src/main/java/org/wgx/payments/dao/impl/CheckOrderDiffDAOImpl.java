package org.wgx.payments.dao.impl;

import java.util.List;

import org.wgx.payments.dao.BaseFrameWorkDao;
import org.wgx.payments.dao.CheckOrderDiffDAO;
import org.wgx.payments.dao.CheckbookItemDAO;
import org.wgx.payments.model.CheckOrderDiffItem;

/**
 * Mybatis based implementation of {@linkplain CheckbookItemDAO}.
 *
 */
public class CheckOrderDiffDAOImpl extends BaseFrameWorkDao<CheckOrderDiffDAO> implements CheckOrderDiffDAO {

    /**
     * {@inheritDoc}
     */
    @Override
    public int save(final CheckOrderDiffItem checkOrderDiffItem) {
        return getMapper().save(checkOrderDiffItem);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CheckOrderDiffItem find(final long id) {
        return getMapper().find(id);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<CheckOrderDiffItem> list(final int limit, final int offset, final int status) {
        return getMapper().list(limit, offset, status);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int updateStatus(final long id, final int status) {
        return getMapper().updateStatus(id, status);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int count(final int status) {
        return getMapper().count(status);
    }

}
