package org.wgx.payments.utils;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import org.apache.commons.lang3.tuple.Pair;

/**
 * author hzxuwei3.
 * date 2017年4月18日 下午7:18:34
 */
public final class DateUtils {

    private static final String DEFAULT_FORMAT = "yyyy-MM-dd HH:mm:ss.SSS";
    private static final SimpleDateFormat FORMATER = new SimpleDateFormat(DEFAULT_FORMAT);
    private static final String FORMAT = "yyyy-MM-dd HH:mm:ss";

    /**
     * 禁止外部实例化.
     */
    private DateUtils() {
    }

    /**
     * 初始化当前处理时间.
     * @param calendar 日历对象.
     * @param dayOfMonth 日偏移量.
     * @return 日历对象.
     */
    public static Calendar initProgressTime(final Calendar calendar, final int dayOfMonth) {
        calendar.add(Calendar.DAY_OF_MONTH, dayOfMonth);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar;
    }

    /**
     * 初始化当前处理完成时间.
     * @param calendar 日历对象.
     * @param dayOfMonth 日偏移量.
     * @return 日历对象.
     */
    public static Calendar initFinishTime(final Calendar calendar, final int dayOfMonth) {
        calendar.add(Calendar.DAY_OF_MONTH, dayOfMonth);
        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 59);
        calendar.set(Calendar.MILLISECOND, 999);
        return calendar;
    }

    /**
     * 初始化当前下次执行时间.
     * @param calendar 日历对象.
     * @param dayOfMonth 日偏移量.
     * @param hourOfDay 小时偏移量.
     * @return 日历对象.
     */
    public static Calendar initNextWorkTime(final Calendar calendar, final int dayOfMonth, final int hourOfDay) {
        initProgressTime(calendar, dayOfMonth);
        calendar.add(Calendar.HOUR_OF_DAY, hourOfDay);
        return calendar;
    }

    /**
     * 获取时间范围.
     * @param progressTime 当前处理时间.
     * @param intervalTime 轮训时间.
     * @return 时间范围组合对象.
     */
    public static Pair<String, String> getTimeRange(final Calendar progressTime, final int intervalTime) {
        SimpleDateFormat formater = new SimpleDateFormat(FORMAT);
        String beginTime = formater.format(progressTime.getTime());
        progressTime.add(Calendar.MINUTE, intervalTime);
        String endTime = formater.format(progressTime.getTime());
        return Pair.of(beginTime, endTime);
    }

    public static String convertFromTimestamp(final Timestamp timestamp) {
        return FORMATER.format(timestamp);
    }
}
