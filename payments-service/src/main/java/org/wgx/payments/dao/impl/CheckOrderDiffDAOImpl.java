package org.wgx.payments.dao.impl;

import java.util.List;

import org.wgx.payments.dao.BaseFrameWorkDao;
import org.wgx.payments.dao.CheckOrderDiffDAO;
import org.wgx.payments.dao.CheckbookItemDAO;
import org.wgx.payments.dao.DAOMethod;
import org.wgx.payments.dao.TableMapping;
import org.wgx.payments.model.CheckOrderDiffItem;

/**
 * Mybatis based implementation of {@linkplain CheckbookItemDAO}.
 *
 */
@TableMapping(table = "CheckOrderDiffItem")
public class CheckOrderDiffDAOImpl extends BaseFrameWorkDao<CheckOrderDiffDAO> implements CheckOrderDiffDAO {

    /**
     * {@inheritDoc}
     */
    @DAOMethod
    @Override
    public int save(final CheckOrderDiffItem checkOrderDiffItem) {
        return getMapper().save(checkOrderDiffItem);
    }

    /**
     * {@inheritDoc}
     */
    @DAOMethod
    @Override
    public CheckOrderDiffItem find(final long id) {
        return getMapper().find(id);
    }

    /**
     * {@inheritDoc}
     */
    @DAOMethod
    @Override
    public List<CheckOrderDiffItem> list(final int limit, final int offset, final int status) {
        return getMapper().list(limit, offset, status);
    }

    /**
     * {@inheritDoc}
     */
    @DAOMethod
    @Override
    public int updateStatus(final long id, final int status) {
        return getMapper().updateStatus(id, status);
    }

    /**
     * {@inheritDoc}
     */
    @DAOMethod
    @Override
    public int count(final int status) {
        return getMapper().count(status);
    }

}
