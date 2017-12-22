package org.wgx.payments.model;

/**
 * author hzxuwei3.
 * date 2017年4月18日 上午10:22:21.
 */
public enum ScheduleJobStatus {
    // CHECKSTYLE:OFF
    INIT("init", "初始"),

    PROCESS("process", "处理中"),

    FINISH("finish", "执行完成"),

    STOP("stop", "停止执行");

    private String code;
    private String desc;

    private ScheduleJobStatus(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public ScheduleJobStatus getByCode(String code) {
        for (ScheduleJobStatus status : values()) {
            if (status.code.equals(code)) {
                return status;
            }
        }

        return null;
    }

    public String getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }
    // CHECKSTYLE:ON
}
