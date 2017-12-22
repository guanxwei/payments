package org.wgx.payments.dao.impl;

import java.util.List;

import org.wgx.payments.dao.ActionRecordDAO;
import org.wgx.payments.dao.BaseFrameWorkDao;
import org.wgx.payments.model.ActionRecord;

/**
 * Mybatis based implementation of {@linkplain ActionRecordDAO}.
 */
public class ActionRecordDAOImpl extends BaseFrameWorkDao implements ActionRecordDAO {

    /**
     * {@inheritDoc}
     */
    @Override
    public int record(final ActionRecord record) {
        ActionRecordDAO mapper = getMapper(ActionRecordDAO.class);
        return process(() -> mapper.record(record));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<ActionRecord> queryByTransactionID(final String transactionID) {
        ActionRecordDAO mapper = getMapper(ActionRecordDAO.class);
        return process(() -> mapper.queryByTransactionID(transactionID));
    }

}
