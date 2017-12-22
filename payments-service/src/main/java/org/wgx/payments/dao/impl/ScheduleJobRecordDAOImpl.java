package org.wgx.payments.dao.impl;

import java.sql.Timestamp;
import java.util.List;

import org.wgx.payments.dao.BaseFrameWorkDao;
import org.wgx.payments.dao.ScheduleJobRecordDAO;
import org.wgx.payments.model.ScheduleJobRecord;

/**
 * Mybatis based implementation of {@link ScheduleJobRecordDAO}.
 *
 */
public class ScheduleJobRecordDAOImpl extends BaseFrameWorkDao implements ScheduleJobRecordDAO {

    /**
     * {@inheritDoc}
     */
    @Override
    public int save(final ScheduleJobRecord scheduleJobRecord) {
        return process(() -> getMapper(ScheduleJobRecordDAO.class).save(scheduleJobRecord));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int update(final ScheduleJobRecord scheduleJobRecord) {
        return process(() -> getMapper(ScheduleJobRecordDAO.class).update(scheduleJobRecord));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<ScheduleJobRecord> getUnfinishScheduleJob(final Timestamp now) {
        return process(() -> getMapper(ScheduleJobRecordDAO.class).getUnfinishScheduleJob(now));
    }

}
