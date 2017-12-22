package org.wgx.payments.model;

import java.sql.Timestamp;

import lombok.Data;

/**
 * author hzxuwei3.
 * date 2017年4月18日 上午10:08:33
 */
@Data
public class ScheduleJobRecord {

    /** 任务执行记录的唯一主键. */
    private long id;

    /**
     * Job logic identity.
     */
    private String jobID;

    /** 创建时间. */
    private Timestamp createTime;

    /** 创建时间. */
    private Timestamp updateTime;

    /** 状态，枚举：ScheduleJobStatus. */
    private String jobStatus;

    /** 下次执行时间. */
    private Timestamp nextWorkTime;

    /** 重试次数. */
    private int retryTimes;

    /** 描述定时任务的功能. */
    private String description;

    /** 扩展信息1. */
    private String ext1;

    /** 扩展信息2. */
    private String ext2;

}
