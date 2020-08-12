package org.wgx.payments.test.dao;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import java.sql.Timestamp;
import java.util.List;

import javax.annotation.Resource;

import org.testng.annotations.Test;
import org.wgx.payments.dao.ScheduleJobRecordDAO;
import org.wgx.payments.model.ScheduleJobRecord;
import org.wgx.payments.model.ScheduleJobStatus;
import org.wgx.payments.tools.ObjectGenerator;

public class ScheduleJobRecordDAOTest extends DAOTestBase {

    @Resource
    private ScheduleJobRecordDAO scheduleJobRecordDAO;

    @Test
    public void save() throws Exception {
        ScheduleJobRecord job = ObjectGenerator.generate(ScheduleJobRecord.class);
        assertEquals(scheduleJobRecordDAO.save(job), 1);
    }

    @Test
    public void update() throws Exception {
        ScheduleJobRecord job = ObjectGenerator.generate(ScheduleJobRecord.class);
        assertEquals(scheduleJobRecordDAO.save(job), 1);
        assertEquals(scheduleJobRecordDAO.update(job), 1);
    }

    @Test
    public void getUnfinishScheduleJob() throws Exception {
        ScheduleJobRecord job = ObjectGenerator.generate(ScheduleJobRecord.class);
        job.setJobStatus(ScheduleJobStatus.INIT.getCode());
        job.setCreateTime(new Timestamp(System.currentTimeMillis()));
        assertEquals(scheduleJobRecordDAO.save(job), 1);
        List<ScheduleJobRecord> records = scheduleJobRecordDAO.getUnfinishScheduleJob(new Timestamp(System.currentTimeMillis()));
        assertTrue(records.size() >= 1);
    }
}
