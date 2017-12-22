package org.wgx.payments.dao;

import java.sql.Timestamp;
import java.util.List;

import org.wgx.payments.model.ScheduleJobRecord;

/**
 * author hzxuwei3.
 * date 2017年4月18日 上午10:06:33
 */
public interface ScheduleJobRecordDAO {

    /**
     * 保存定时任务执行记录.
     * @param scheduleJobRecord 定时任务记录
     * @return 插入条数
     */
    int save(final ScheduleJobRecord scheduleJobRecord);

    /**
     * 根据id更新状态.
     * @param scheduleJobRecord 对账任务记录
     * @return true：成功，false：失败
     */
    int update(final ScheduleJobRecord scheduleJobRecord);

    /**
     * 获取未完成的对账任务记录，重新处理.
     * @return 对账任务记录列表
     */
    List<ScheduleJobRecord> getUnfinishScheduleJob(final Timestamp now);
}
