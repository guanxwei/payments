package org.wgx.payments.test.dao;

import org.wgx.payments.dao.ActionRecordDAO;
import org.wgx.payments.model.ActionRecord;
import org.wgx.payments.transaction.Transaction;

import lombok.Data;

@Data
public class TestDaoService {

    private ActionRecordDAO actionRecordDAO;

    @Transaction
    public void insert(final ActionRecord record) {
        actionRecordDAO.record(record);
        throw new RuntimeException();
    }

    @Transaction
    public void insertWithoutError(final ActionRecord record) {
        actionRecordDAO.record(record);
    }
}
