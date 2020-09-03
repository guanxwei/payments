package org.wgx.payments.dao.impl;

import java.util.List;

import org.wgx.payments.dao.ActionRecordDAO;
import org.wgx.payments.dao.BaseFrameWorkDao;
import org.wgx.payments.dao.DAOMethod;
import org.wgx.payments.dao.TableMapping;
import org.wgx.payments.model.ActionRecord;

/**
 * Mybatis based implementation of {@linkplain ActionRecordDAO}.
 */
@TableMapping(table = "ActionRecord", insertMethod = "record")
public class ActionRecordDAOImpl extends BaseFrameWorkDao<ActionRecordDAO> implements ActionRecordDAO {

    /**
     * {@inheritDoc}
     */
    @Override
    @DAOMethod
    public int record(final ActionRecord record) {
        return getMapper().record(record);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @DAOMethod
    public List<ActionRecord> queryByTransactionID(final String transactionID) {
        return getMapper().queryByTransactionID(transactionID);
    }

}
