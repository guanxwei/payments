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
public class CheckOrderDiffDAOImpl extends BaseFrameWorkDao implements CheckOrderDiffDAO {

    /**
     * {@inheritDoc}
     */
    @Override
    public int save(final CheckOrderDiffItem checkOrderDiffItem) {
        return process(() -> getMapper(CheckOrderDiffDAO.class).save(checkOrderDiffItem));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CheckOrderDiffItem find(final long id) {
        return process(() -> getMapper(CheckOrderDiffDAO.class).find(id));

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<CheckOrderDiffItem> list(final int limit, final int offset, final int status) {
        return process(() -> getMapper(CheckOrderDiffDAO.class).list(limit, offset, status));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int updateStatus(final long id, final int status) {
        return process(() -> getMapper(CheckOrderDiffDAO.class).updateStatus(id, status));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int count(final int status) {
        return process(() -> getMapper(CheckOrderDiffDAO.class).count(status));
    }

}
