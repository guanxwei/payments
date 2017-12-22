package org.wgx.payments.job;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.annotation.Resource;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.util.CollectionUtils;
import org.wgx.payments.client.api.helper.BusinessProfile;
import org.wgx.payments.client.api.helper.PaymentMethod;
import org.wgx.payments.client.api.helper.PaymentOperation;
import org.wgx.payments.client.api.helper.PaymentResponseStatus;
import org.wgx.payments.dao.CheckOrderDiffDAO;
import org.wgx.payments.dao.CheckbookItemDAO;
import org.wgx.payments.dao.FastSearchTableDAO;
import org.wgx.payments.dao.PaymentRequestDAO;
import org.wgx.payments.dao.PaymentResponseDAO;
import org.wgx.payments.dao.PaymentStatisticDAO;
import org.wgx.payments.dao.ScheduleJobRecordDAO;
import org.wgx.payments.model.CheckOrderDiffItem;
import org.wgx.payments.model.CheckbookIssueItemStatus;
import org.wgx.payments.model.CheckbookItem;
import org.wgx.payments.model.FastSearchTableItem;
import org.wgx.payments.model.FastSearchTableItemStatus;
import org.wgx.payments.model.PaymentRequest;
import org.wgx.payments.model.PaymentResponse;
import org.wgx.payments.model.PaymentStatistic;
import org.wgx.payments.model.ScheduleJobRecord;
import org.wgx.payments.model.ScheduleJobStatus;
import org.wgx.payments.utils.DateUtils;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

/**
 * author hzxuwei3.
 * date 2017-4-17 19:52:51
 */
@Slf4j
@EnableScheduling
public class OrderSerialCheckingJob {
    @Resource
    @Setter
    private CheckbookItemDAO checkbookItemDAO;

    @Resource
    @Setter
    private PaymentResponseDAO paymentResponseDAO;

    @Resource
    @Setter
    private PaymentRequestDAO paymentRequestDAO;

    @Resource
    @Setter
    private CheckOrderDiffDAO checkOrderDiffDAO;

    @Resource
    @Setter
    private ScheduleJobRecordDAO scheduleJobRecordDao;

    @Resource @Setter
    private PaymentStatisticDAO paymentStatisticDAO;

    @Resource @Setter
    private FastSearchTableDAO fastSearchTableDAO;

    /** 日对账的子周期，单位：分. */
    @Setter
    private static int periodIntervalMinute = 10;

    /** 日对账周期偏移量（核对T-1日的账单）. */
    private static int dayOffset = -1;

    /** 对账重试间隔，单位：小时. */
    private static int retryIntervalHour = 4;

    /** 最大重试次数. */
    private static int maxRetryTimes = 9;

    /**
     * 每天下午16：00触发对账任务，核对T-1日账单.
     */
    @Scheduled(cron = "0 0 16 * * ?")
    public void doWork() {
        long startTimeMillis = System.currentTimeMillis();
        log.info("开始对账");

        // 落地对账任务记录，init状态
        ScheduleJobRecord record = initScheduleJobRecord();
        scheduleJobRecordDao.save(record);

        String checkbookDownloadJob = "Job::" + new SimpleDateFormat("yyyyMMdd").format(new Date(System.currentTimeMillis()));

        try {
            // 对账前先检查账单下载任务是否已经完成.
            if (!isDownloadjobCompleted(checkbookDownloadJob)) {
                log.warn("对账单下载任务未执行完成,本次对账任务延后执行");
                throw new RuntimeException("对账单下载任务未执行完成,本次对账任务延后执行");
            }

            // 执行对账（核对T-1日的账单）
            doCheck(record.getJobID(), DateUtils.initProgressTime(Calendar.getInstance(), dayOffset),
                    DateUtils.initFinishTime(Calendar.getInstance(), dayOffset));

            // 更新对账任务记录状态，finish状态
            record.setJobStatus(ScheduleJobStatus.FINISH.getCode());
            record.setUpdateTime(new Timestamp(Calendar.getInstance().getTimeInMillis()));
            scheduleJobRecordDao.update(record);
        } catch (Exception e) {
            log.error("对账过程发生异常", e);

            // 设置重试时间
            record.setJobStatus(ScheduleJobStatus.PROCESS.getCode());
            record.setUpdateTime(new Timestamp(Calendar.getInstance().getTimeInMillis()));
            Calendar nextWorkTime = Calendar.getInstance();
            nextWorkTime.add(Calendar.HOUR_OF_DAY, retryIntervalHour);
            record.setNextWorkTime(new Timestamp(nextWorkTime.getTimeInMillis()));
            record.setExt1(checkbookDownloadJob);
            scheduleJobRecordDao.update(record);
        }

        log.info("本次对账结束，耗时：[{}]ms", System.currentTimeMillis() - startTimeMillis);
    }

    private boolean isDownloadjobCompleted(final String job) {
        FastSearchTableItem item = fastSearchTableDAO.find(job);
        if (item == null || item.getStatus() != FastSearchTableItemStatus.PROCESSED.status()) {
            return false;
        }
        return true;
    }

    /**
     * 定时每隔30分钟检查一次未执行完的定时任务记录，并重新执行.
     */
    @Scheduled(cron = "0 0/30 * * * ?")
    public void deamon() {
        log.info("尝试未完成的对账任务");
        List<ScheduleJobRecord> recordList = scheduleJobRecordDao.getUnfinishScheduleJob(new Timestamp(System.currentTimeMillis()));
        if (!CollectionUtils.isEmpty(recordList)) {
            Calendar curTime = Calendar.getInstance();
            for (ScheduleJobRecord record : recordList) {
                // 依旧先检查对账单下载任务是否执行完成,对账单下载完成才执行.
                if (!isDownloadjobCompleted(record.getExt1())) {
                    log.info("对账单下载任务[{}]还没执行完成,本次对账延后执行", record.getExt1());
                    continue;
                }

                log.info("开始重新执行对账任务：[{}]", record.getId());
                // 重试次数超过9次时，设置stop状态
                if (record.getRetryTimes() >= maxRetryTimes) {
                    record.setJobStatus(ScheduleJobStatus.STOP.getCode());
                    record.setUpdateTime(new Timestamp(curTime.getTimeInMillis()));
                    record.setRetryTimes(record.getRetryTimes() + 1);
                    scheduleJobRecordDao.update(record);
                    continue;
                }

                // 如果设置了下次执行时间，则当前时间必须超过下次执行时间才能执行
                if (record.getNextWorkTime().after(curTime.getTime())) {
                    continue;
                }

                // 初始化任务创建的时间
                Calendar jobCalendar = Calendar.getInstance();
                jobCalendar.setTime(record.getCreateTime());

                // 执行对账
                doCheck(record.getJobID(), DateUtils.initProgressTime(jobCalendar, dayOffset),
                        DateUtils.initFinishTime(jobCalendar, dayOffset));

                // 更新对账任务记录状态，finish状态
                record.setJobStatus(ScheduleJobStatus.FINISH.getCode());
                record.setUpdateTime(new Timestamp(Calendar.getInstance().getTimeInMillis()));
                record.setRetryTimes(record.getRetryTimes() + 1);
                scheduleJobRecordDao.update(record);

                log.info("结束重新执行对账任务：[{}]", record.getId());
            }
        }
    }

    /**
     * 捞取指定的时间范围内的账单进行对账，未防止账单记录过多，每10分钟划分为一个子周期.
     * @param progressTime
     *            当前处理时间
     * @param finishTime
     *            周期截止时间
     */
    private void doCheck(final String jobId, final Calendar progressTime, final Calendar finishTime) {
        // 当前对账列表
        List<PaymentResponse> checkingList = null;
        // 对账过程中的差异列表
        List<PaymentResponse> checkingDiffList = new CopyOnWriteArrayList<PaymentResponse>();
        // 第三方对账单
        Map<String, List<CheckbookItem>> checkbookItemMap = new HashMap<String, List<CheckbookItem>>();

       ConcurrentMap<String, BigDecimal> statistic = new ConcurrentHashMap<>();

       for (BusinessProfile profile : BusinessProfile.values()) {
           for (PaymentMethod method : PaymentMethod.values()) {
               for (PaymentOperation operation : PaymentOperation.values()) {
                   String statisticKey = StringUtils.join(new Object[] {
                           profile.profile(), method.paymentMethodName(), operation.operationType()
                           }, "_");
                   statistic.putIfAbsent(statisticKey, new BigDecimal(0));
               }
           }
       }

        while (progressTime.before(finishTime)) {
            // 查询原支付订单响应记录
            Pair<String, String> timeRangePair = DateUtils.getTimeRange(progressTime, periodIntervalMinute);
            log.info("当前对账子周期，[{}-{}]", timeRangePair.getLeft(), timeRangePair.getRight());
            checkingList = paymentResponseDAO.getPaymentResponseListByRange(timeRangePair.getLeft(),
                    timeRangePair.getRight());

            List<CheckbookItem> checkbookItems = checkbookItemDAO.getCheckbookItemsByRange(timeRangePair.getLeft(),
                    timeRangePair.getRight());
            addToItemMap(checkbookItemMap, checkbookItems);

            // 如果对账列表和差异列表都是空，则进入下一对账周期
            if (CollectionUtils.isEmpty(checkingList) && CollectionUtils.isEmpty(checkingDiffList)) {
                continue;
            }

            // 核对一下上一对账周期的差异记录
            for (int i = 0; i < checkingDiffList.size(); i++) {
                if (isMatched(checkingDiffList.get(i), checkbookItemMap)) {
                    checkingDiffList.remove(i);
                }
            }

            //核对本周期订单
            for (PaymentResponse resp : checkingList) {
                //登记统计信息
                if (resp.getAcknowledgedAmount() != null && resp.getStatus() == PaymentResponseStatus.SUCCESS.status()
                        && (PaymentOperation.CHARGE.operationType().equals(resp.getOperationType())
                                || PaymentOperation.REFUND.operationType().equals(resp.getOperationType())
                                || PaymentOperation.SCHEDULEDPAY.operationType().equals(resp.getOperationType()))) {
                    BusinessProfile profile = BusinessProfile.fromProfile(resp.getBusiness());
                    String statisticKey = StringUtils.join(new Object[] {
                            profile.profile(), resp.getPaymentMethod(), resp.getOperationType()
                            }, "_");
                    BigDecimal raw = statistic.get(statisticKey);
                    statistic.put(statisticKey, raw.add(new BigDecimal(resp.getAcknowledgedAmount())));
                }

                // 如果是签约、解约等操作类型则不核对，忽略
                if (isInvalidType(resp.getOperationType())) {
                    continue;
                }

                // 如果为匹配成功加入差异列表
                if (!isMatched(resp, checkbookItemMap)) {
                    checkingDiffList.add(resp);
                }
            }
        }

        // 映射表中如果还存在记录，说明是未匹配到的部分，保存到差异表
        if (!CollectionUtils.isEmpty(checkbookItemMap)) {
            for (Entry<String, List<CheckbookItem>> entry : checkbookItemMap.entrySet()) {
                for (CheckbookItem item : entry.getValue()) {
                    checkOrderDiffDAO.save(transToCheckOrderDiffItem(jobId, item));
                }
            }
        }

        // 核对全部完成后，保存差异记录
        if (!CollectionUtils.isEmpty(checkingDiffList)) {
            for (PaymentResponse resp : checkingDiffList) {
                checkOrderDiffDAO.save(transToCheckOrderDiffItem(jobId, resp));
            }
        }

        SimpleDateFormat wechatFormat = new SimpleDateFormat("yyyyMMdd");

        // 保存统计信息
        for (Entry<String, BigDecimal> entry : statistic.entrySet()) {
            String[] keys = entry.getKey().split("_");
            PaymentStatistic statisticItem = new PaymentStatistic();
            Double value = entry.getValue().doubleValue();
            statisticItem.setAmount(String.format("%.1f", value));
            statisticItem.setBusiness(keys[0]);
            statisticItem.setPaymentMethod(keys[1]);
            statisticItem.setPaymentOperation(keys[2]);
            Date date = progressTime.getTime();
            statisticItem.setDate(Integer.parseInt(wechatFormat.format(date)));
            statisticItem.setTarget("Netease");
            if (value > 0.1) {
                paymentStatisticDAO.save(statisticItem);
            }
        }
    }

    /**
     * 初始化对账任务记录.
     * @return 对账任务记录
     */
    private ScheduleJobRecord initScheduleJobRecord() {
        ScheduleJobRecord record = new ScheduleJobRecord();
        record.setJobID(UUID.randomUUID().toString().replace("-", ""));
        record.setCreateTime(new Timestamp(Calendar.getInstance().getTimeInMillis()));
        record.setUpdateTime(new Timestamp(Calendar.getInstance().getTimeInMillis()));
        record.setNextWorkTime(new Timestamp(
                DateUtils.initNextWorkTime(Calendar.getInstance(), dayOffset, retryIntervalHour).getTimeInMillis()));
        record.setRetryTimes(0);
        record.setJobStatus(ScheduleJobStatus.INIT.getCode());
        record.setDescription(ScheduleJobStatus.INIT.getDesc());
        return record;
    }

    /**
     * 转化为Map，便于后面核对查找.
     * @param checkbookItemMap
     * @param checkbookItems
     */
    private void addToItemMap(final Map<String, List<CheckbookItem>> checkbookItemMap,
            final List<CheckbookItem> checkbookItems) {
        if (CollectionUtils.isEmpty(checkbookItems)) {
            return;
        }

        for (CheckbookItem item : checkbookItems) {
            String key = item.getTransactionID() + item.getType();
            if (checkbookItemMap.containsKey(key)) {
                checkbookItemMap.get(key).add(item);
            } else {
                List<CheckbookItem> itemList = new CopyOnWriteArrayList<CheckbookItem>();
                itemList.add(item);
                checkbookItemMap.put(key, itemList);
            }
        }
    }

    /**
     * 转化为对账差异记录.
     * @param jobId
     *            任务ID
     * @param item
     *            第三方对账单记录
     * @return 对账差异记录
     */
    private CheckOrderDiffItem transToCheckOrderDiffItem(final String jobId, final CheckbookItem item) {
        CheckOrderDiffItem itemDiff = new CheckOrderDiffItem();
        itemDiff.setJobId(jobId);
        itemDiff.setTransactionID(item.getTransactionID());
        itemDiff.setCreateTime(new Timestamp(Calendar.getInstance().getTimeInMillis()));
        itemDiff.setLastUpdateTime(item.getTransactionTime());
        itemDiff.setOperationType(item.getType());
        itemDiff.setPaymentMethod(item.getPaymentMethod());
        itemDiff.setAcknowledgedAmount(item.getAcknowledgedAmount());
        itemDiff.setReferenceID(item.getReferenceID());
        itemDiff.setStatus(CheckbookIssueItemStatus.FAIL.status());
        itemDiff.setBusiness(item.getBusiness());
        return itemDiff;
    }

    /**
     * 转化为对账差异记录.
     * @param jobId
     *            任务ID
     * @param resp
     *            原支付响应记录
     * @return 对账差异记录
     */
    private CheckOrderDiffItem transToCheckOrderDiffItem(final String jobId, final PaymentResponse resp) {
        CheckOrderDiffItem itemDiff = new CheckOrderDiffItem();
        itemDiff.setJobId(jobId);
        itemDiff.setTransactionID(resp.getTransactionID());
        itemDiff.setCreateTime(new Timestamp(Calendar.getInstance().getTimeInMillis()));
        itemDiff.setLastUpdateTime(resp.getLastUpdateTime());
        itemDiff.setOperationType(resp.getOperationType());
        itemDiff.setPaymentMethod(resp.getPaymentMethod());
        itemDiff.setAcknowledgedAmount(resp.getAcknowledgedAmount());
        itemDiff.setReferenceID(resp.getReferenceID());
        itemDiff.setStatus(CheckbookIssueItemStatus.FAIL.status());
        itemDiff.setBusiness(resp.getBusiness());
        itemDiff.setCustomerID(resp.getCustomerID());
        return itemDiff;
    }

    /**
     * 支付结果与第三方对账单是否匹配.
     * @param resp
     *            支付结果
     * @param checkbookItemMap
     *            第三方对账单
     * @return true：匹配成功，false：匹配失败
     */
    private boolean isMatched(final PaymentResponse resp, final Map<String, List<CheckbookItem>> checkbookItemMap) {
        String transactionID = resp.getTransactionID();
        if (StringUtils.equals(resp.getPaymentMethod(), PaymentMethod.ALIPAY.paymentMethodName())
                && StringUtils.equals(PaymentOperation.REFUND.operationType(), resp.getOperationType())) {
            PaymentRequest request = paymentRequestDAO.getPaymentRequestByTransactionID(resp.getTransactionID());
            transactionID = request.getParentID();
        }

        log.info("执行核对，[{}-{}]", resp.getTransactionID(), resp.getOperationType());
        boolean isMatched = false;
        String key = transactionID + resp.getOperationType();
        List<CheckbookItem> itemList = checkbookItemMap.get(key);
        if (!CollectionUtils.isEmpty(itemList)) {
            for (int i = 0; i < itemList.size(); i++) {
                CheckbookItem item = itemList.get(i);
                if ((item != null) && (item.getStatus() == resp.getStatus())
                        && StringUtils.equals(item.getAcknowledgedAmount(), resp.getAcknowledgedAmount())) {
                    log.info("核对匹配成功，[{}-{}-{}]", key, item.getStatus(), item.getAcknowledgedAmount());
                    isMatched = true;
                    itemList.remove(i);
                    break;
                }
                if (item != null && StringUtils.equals(item.getAcknowledgedAmount(), resp.getAcknowledgedAmount())) {
                    itemList.remove(i);
                }
            }
        }

        // 如果对账单明细为空，则删除映射表中记录
        if (CollectionUtils.isEmpty(itemList)) {
            checkbookItemMap.remove(key);
        }

        return isMatched;
    }

    /**
     * 检查操作类型是否为无需核对的类型，目前签约、解约无需核对.
     * @param operationType 操作类型
     * @return true：不核对类型，false：需核对类型
     */
    private boolean isInvalidType(final String operationType) {
        if (StringUtils.equals(PaymentOperation.SIGN.name(), operationType)
                || StringUtils.equals(PaymentOperation.RESCIND.name(), operationType)) {
            return true;
        }
        return false;
    }

}
