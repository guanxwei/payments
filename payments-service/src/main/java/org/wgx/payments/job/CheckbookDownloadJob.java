package org.wgx.payments.job;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import javax.annotation.Resource;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.wgx.payments.client.api.helper.BusinessProfile;
import org.wgx.payments.client.api.helper.PaymentMethod;
import org.wgx.payments.client.api.helper.PaymentOperation;
import org.wgx.payments.client.api.helper.PaymentResponseStatus;
import org.wgx.payments.dao.ActionRecordDAO;
import org.wgx.payments.dao.CheckbookItemDAO;
import org.wgx.payments.dao.FastSearchTableDAO;
import org.wgx.payments.dao.PaymentRequestDAO;
import org.wgx.payments.dao.PaymentStatisticDAO;
import org.wgx.payments.facade.Facade;
import org.wgx.payments.model.CheckbookItem;
import org.wgx.payments.model.FastSearchTableItem;
import org.wgx.payments.model.FastSearchTableItemStatus;
import org.wgx.payments.model.PaymentStatistic;
import org.wgx.payments.signature.Account;
import org.wgx.payments.signature.AccountFactory;
import org.wgx.payments.signature.SignatureGenerator;
import org.wgx.payments.tools.Jackson;
import org.wgx.payments.utils.AccountUtils;
import org.wgx.payments.utils.AlipayConstants;
import org.wgx.payments.utils.AlipayUtils;
import org.wgx.payments.utils.WechatConstants;
import org.wgx.payments.utils.WechatSignatureHelper;
import org.wgx.payments.utils.XMLUtils;

import com.google.common.util.concurrent.AtomicDouble;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

/**
 * Scheduled bill checkbook download job.
 *
 */
@Slf4j
@EnableScheduling
public class CheckbookDownloadJob {

    @Setter @Resource
    private Facade<Pair<String, String>, String> alipayFacade;

    @Resource @Setter
    private Facade<Pair<String, String>, String> wechatFacade;

    @Resource @Setter
    private PaymentRequestDAO paymentRequestDAO;

    private SimpleDateFormat wechatFormat = new SimpleDateFormat("yyyyMMdd");
    private SimpleDateFormat alipayFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    @Resource @Setter
    private CheckbookItemDAO checkbookItemDAO;

    @Resource @Setter
    private ActionRecordDAO actionRecordDAO;

    @Resource @Setter
    private PaymentStatisticDAO paymentStatisticDAO;

    @Resource @Setter
    private FastSearchTableDAO fastSearchTableDAO;

    @Resource @Setter
    private AccountFactory accountFactory;

    // Initiate the statistic map.
    private ThreadLocal<ConcurrentMap<String, AtomicDouble>> statistic = new ThreadLocal<>();

    /**
     * Retry failed job.
     */
    public void retry() {
        ScheduledExecutorService service = Executors.newScheduledThreadPool(1);
        Runnable runner = new Runnable() {
            @Override
            public void run() {
                try {
                    log.info("Retry job initiate to work!");
                    List<FastSearchTableItem> items = fastSearchTableDAO.list("CheckbookDownlodRetry", FastSearchTableItemStatus.PROCESSING.status());
                    if (items == null || items.isEmpty()) {
                        log.info("No pending on retry job, nice day!");
                        return;
                    }
                    statistic.set(new ConcurrentHashMap<>());
                    for (FastSearchTableItem item : items) {
                        retry(item);
                        statistic.get().clear();
                    }
                } catch (Exception e) {
                    log.info("Job retry failed due to ", e);
                }
            }
        };
        service.scheduleAtFixedRate(runner, 1, 60, TimeUnit.MINUTES);
    }

    /**
     * Initiate the checkbook download job.
     */
    @Scheduled(cron = "0 15 18 * * ?")
    public void init() {
        // Clear statistic data for this new round.
        log.info("Checkbook down load job begin to work at time [{}]", new Timestamp(System.currentTimeMillis()));
        statistic.set(new ConcurrentHashMap<>());

        // Prepare job context.
        log.info("Prepare necessary material for the job");
        prepare();
        long id = initiateJob();
        log.info("New job [{}] initiated!", id);

        runDownloadJob(yesterday(), new Date(System.currentTimeMillis() - 86400000));

        // Save statistic data in DB.
        markJobAsCompleted(id);
    }

    private void retry(final FastSearchTableItem job) {
        log.info("Begin to rerun checkbook down load job for date [{}]", job.getTransactionID());
        Date yesterday = calculateDate(job);
        prepare();

        // Download checkbook from Alipay.
        log.info("Begin to reload check book for Alipay and Wechat for date [{}]", job.getTransactionID(),
                new Timestamp(System.currentTimeMillis()));
        runDownloadJob(yesterday, yesterday);
        log.info("Checkbook reload job for date [{}] completed", job.getTransactionID(),
                new Timestamp(System.currentTimeMillis()));

        markRetryJobAsComplete(yesterday, job);
    }

    private void runDownloadJob(final Date alipayStyleYesterday, final Date wechatStyleYesterday) {
       // Download checkbook from Wechat.
       log.info("Begin to download checkbook from Wechat");
       String currentAccount = null;
       String predecessor = "";
       try {
           for (String accountName : accountFactory.getWechatAccountList()) {
               Account account = accountFactory.getAccount(accountName);
               if (predecessor.equals(account.getAdditional())) {
                   continue;
               }
               predecessor = account.getAdditional();
               currentAccount = accountName;
               AccountUtils.set(account);
               checkWechat(wechatStyleYesterday, accountName);
           }
       } catch (Exception e) {
           log.error("Fail to connect Wechat to download checkbook list for account [{}]", currentAccount);
           log.error("Error", e);
           return;
       }
       log.info("Wechat checkbook downloaded at time", new Timestamp(System.currentTimeMillis()));

       // Download checkbook from Alipay.
      log.info("Begin to download check book for Alipay at time", new Timestamp(System.currentTimeMillis()));
      predecessor = "";
      try {
          for (String accountName : accountFactory.getAlipayAccountList()) {
              Account account = accountFactory.getAccount(accountName);
              if (predecessor.equals(account.getAdditional())) {
                  continue;
              }
              predecessor = account.getAdditional();
              currentAccount = accountName;
              AccountUtils.set(account);
              checkAlipay(alipayStyleYesterday, accountName);
          }
      } catch (Exception e) {
          log.error("Fail to connect Alipay to download checkbook list for acccount", currentAccount);
          log.error("Error", e);
          return;
      }
      log.info("Alipay checkbook downloaded at time", new Timestamp(System.currentTimeMillis()));
   }

    private void saveStatisticData(final Date date) {
        long now = System.currentTimeMillis();
        Date yesterday;
        if (date == null) {
            yesterday = new Date(now - 86400000);
        } else {
            yesterday = date;
        }
        for (Entry<String, AtomicDouble> entry : statistic.get().entrySet()) {
            String[] keys = entry.getKey().split("_");
            PaymentStatistic item = new PaymentStatistic();
            Double value = entry.getValue().doubleValue();
            item.setAmount(String.format("%.1f", value));
            item.setBusiness(keys[0]);
            item.setPaymentMethod(keys[1]);
            item.setPaymentOperation(keys[2]);
            item.setDate(Integer.parseInt(wechatFormat.format(yesterday)));
            item.setTarget("Partner");
            if (value > 0.1) {
                paymentStatisticDAO.save(item);
            }
        }
    }

    private Date yesterday() {
        Date date = new Date();
        Date yesterday;
        GregorianCalendar gc = new GregorianCalendar();
        gc.setTime(date);
        if ((gc.get(Calendar.HOUR_OF_DAY) == 0) && (gc.get(Calendar.MINUTE) == 0)
                && (gc.get(Calendar.SECOND) == 0)) {
            yesterday =  new Date(date.getTime() - (24 * 60 * 60 * 1000));
        } else {
            yesterday = new Date(date.getTime() - 60L * gc.get(Calendar.HOUR_OF_DAY) * 60 * 1000 - 1000L * gc.get(Calendar.MINUTE) * 60
                    - 1000L * gc.get(Calendar.SECOND) - 24L * 60 * 60 * 1000);
        }
        return yesterday;
    }

    private Date calculateDate(final FastSearchTableItem item) {
        String date = item.getTransactionID();
        int year = Integer.parseInt(date.substring(0, 4));
        int month = Integer.parseInt(date.substring(4, 6));
        int day = Integer.parseInt(date.substring(6, 8));
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, year);
        calendar.set(Calendar.MONTH, month - 1);
        calendar.set(Calendar.DAY_OF_MONTH, day);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTime();
    }

    private void prepare() {
        for (BusinessProfile profile : BusinessProfile.values()) {
            for (PaymentMethod method : PaymentMethod.values()) {
                for (PaymentOperation operation : PaymentOperation.values()) {
                    String statisticKey = StringUtils.join(new Object[] {
                            profile.profile(), method.paymentMethodName(), operation.operationType()
                            }, "_");
                    statistic.get().putIfAbsent(statisticKey, new AtomicDouble(0));
                }
            }
        }
    }

    private long initiateJob() {
        FastSearchTableItem job = new FastSearchTableItem();
        job.setItemKey("Job::" + wechatFormat.format(new Date(System.currentTimeMillis())));
        job.setStatus(FastSearchTableItemStatus.PROCESSING.status());
        job.setTime(new Timestamp(System.currentTimeMillis()));
        job.setTransactionID(job.getItemKey());
        job.setMessage("对账单下载任务");
        fastSearchTableDAO.save(job);
        log.info("Job [{}] initiated ", Jackson.json(job));
        return job.getId();
    }

    private void markRetryJobAsComplete(final Date yesterday, final FastSearchTableItem job) {
        saveStatisticData(yesterday);
        FastSearchTableItem raw = fastSearchTableDAO.find("Job::" + job.getTransactionID());
        fastSearchTableDAO.tryUpdateStatus(FastSearchTableItemStatus.RETRYING.status(),
                FastSearchTableItemStatus.PROCESSED.status(), raw.getId());
        fastSearchTableDAO.deleteItem(job.getId());
        log.info("Checkbook download job retry for date [{}] complete at time [{}]", job.getTransactionID(),
                new Timestamp(System.currentTimeMillis()));
    }

    private void markJobAsCompleted(final long jobId) {
        saveStatisticData(null);
        fastSearchTableDAO.tryUpdateStatus(FastSearchTableItemStatus.PROCESSING.status(),
                FastSearchTableItemStatus.PROCESSED.status(), jobId);
    }

         ////////////////////////////////////////////////////////////////////
        //                                                                //
       //                Alipay checkbook download procedure             //
      //                                                                //
     //                                                                //
    ////////////////////////////////////////////////////////////////////
    private void checkAlipay(final Date yesterday, final String account) throws Exception {
        AtomicInteger total = new AtomicInteger(0);
        for (int i = 0; i < 288; i++) {
            log.info("Begin to load Round [{}] for Alipay", i);
            long beginTime = yesterday.getTime() + 300000L * i;
            long endTime = yesterday.getTime() + 300000L * (i + 1);
            String url = AlipayConstants.GATEWAY_URL + generateAlipayCheckbookDownloadURL(beginTime, endTime, account);
            log.debug("Alipay bill checking URL : [{}] for round [{}]", url, i);

            Pair<String, String> pair = Pair.of(url, null);
            String result = alipayFacade.call(pair);
            parseAlipayResponse(result, total);
        }
        log.info("Totally: [{}] trasactions paied by Alipay date [{}]",
                total, new SimpleDateFormat("yyyy-MM-dd").format(yesterday));
    }

    private String generateAlipayCheckbookDownloadURL(final long beginTime, final long endTime, final String account) {
        SortedMap<String, String> map = new TreeMap<>();
        map.put("service", "account.page.query");
        map.put("partner", AccountUtils.get().getAdditional());
        map.put("_input_charset", AlipayConstants.INPUT_CHARSET);
        int page = 1;
        map.put("gmt_start_time", alipayFormat.format(new Date(beginTime)));
        map.put("gmt_end_time", alipayFormat.format(new Date(endTime)));
        map.put("page_no", Integer.toString(page));
        return AlipayUtils.buildRequestParaStr(map, false,
                accountFactory.getPrivateKeyByMaterialName(accountFactory.getMaterialNameByAccountName(account)));
    }

    private void parseAlipayResponse(final String result, final AtomicInteger total) throws Exception {
        Document document = DocumentHelper.parseText(result);
        @SuppressWarnings("unchecked")
        List<Element> elements = document.getRootElement().element("response").element("account_page_query_result")
                .element("account_log_list").elements("AccountQueryAccountLogVO");

        elements.parallelStream()
            .filter(element -> {
                return !"收费".equals(element.element("trans_code_msg").getText());
            })
            .collect(Collectors.toList())
            .forEach(element -> {
                Pair<CheckbookItem, BusinessProfile> pair = prepareAlipayCheckItem(element);
                CheckbookItem item = pair.getLeft();
                BusinessProfile profile = pair.getRight();
                String business = null;
                if ("交易退款".equals(element.element("sub_trans_code_msg").getText())) {
                    processAlipayRefundItem(item, profile, element, business, total);
                } else if ("在线支付".equals(element.element("trans_code_msg").getText())) {
                    processAlipayChargeItem(item, profile, element, business, total);
                }
            });
    }

    private Pair<CheckbookItem, BusinessProfile> prepareAlipayCheckItem(final Element element) {
        CheckbookItem item = new CheckbookItem();
        item.setStatus(PaymentResponseStatus.SUCCESS.status());
        item.setRecordTime(Timestamp.valueOf(LocalDateTime.now()));
        item.setPaymentMethod(PaymentMethod.ALIPAY.paymentMethodName());
        String displayName =  element.element("goods_title").getText();
        BusinessProfile profile = BusinessProfile.fromShowName(displayName);
        item.setBusiness(profile.profile());
        Pair<CheckbookItem, BusinessProfile> pair = Pair.of(item, profile);
        return pair;
    }

    private void processAlipayChargeItem(final CheckbookItem item, final BusinessProfile profile, final Element element,
            final String business, final AtomicInteger total) {
        // Charge record.
        item.setType(PaymentOperation.CHARGE.operationType());
        if (BusinessProfile.AUTO_VIP.equals(profile)) {
            item.setType(PaymentOperation.SCHEDULEDPAY.operationType());
        }
        item.setAcknowledgedAmount(element.element("total_fee").getText());
        item.setTransactionID(element.element("merchant_out_order_no").getText());
        item.setExternalTransactionID(element.element("trade_no").getText());
        item.setUniqueKey(element.element("iw_account_log_id").getText() + item.getTransactionID());
        item.setTransactionTime(Timestamp.valueOf(element.element("trans_date").getText()));
        double fee = Double.parseDouble(item.getAcknowledgedAmount());
        //business = paymentRequestDAO.getPaymentRequestByTransactionID(item.getTransactionID()).getBusiness();
        String statisticKey = StringUtils.join(new Object[] {
                business == null ? profile.profile() : business,
                PaymentMethod.ALIPAY.paymentMethodName(), PaymentOperation.CHARGE.operationType()
            }, "_");
        AtomicDouble totalCharge = statistic.get().get(statisticKey);
        totalCharge.addAndGet(fee);
        total.incrementAndGet();
        // We'll have to filter out the payment transactions processed by the legacy store system.
        if (item.getTransactionID().length() == 32 && checkbookItemDAO.findByUniqueItem(item.getUniqueKey()) == null
                && !item.getTransactionID().contains("_")) {
            checkbookItemDAO.save(item);
        }
    }

    private void processAlipayRefundItem(final CheckbookItem item, final BusinessProfile profile, final Element element,
            final String business, final AtomicInteger total) {
        // Refund record.
        item.setType(PaymentOperation.REFUND.operationType());
        item.setAcknowledgedAmount(element.element("trade_refund_amount").getText());
        item.setTransactionID(element.element("merchant_out_order_no").getText());
        item.setExternalTransactionID(element.element("trade_no").getText());
        item.setUniqueKey(element.element("iw_account_log_id").getText() + item.getTransactionID());
        item.setTransactionTime(Timestamp.valueOf(element.element("trans_date").getText()));
        // We'll have to filter out the payment transactions processed by the legacy store system.
        double fee = Double.parseDouble(item.getAcknowledgedAmount());
        //business = paymentRequestDAO.getPaymentRequestByTransactionID(item.getTransactionID()).getBusiness();
        String statisticKey = StringUtils.join(new Object[] {
                business == null ? profile.profile() : business,
                PaymentMethod.ALIPAY.paymentMethodName(), PaymentOperation.REFUND.operationType()
            }, "_");
        AtomicDouble totalRefund = statistic.get().get(statisticKey);
        totalRefund.addAndGet(fee);
        total.incrementAndGet();
        if (item.getTransactionID().length() == 32 && checkbookItemDAO.findByUniqueItem(item.getUniqueKey()) == null
                && !item.getTransactionID().contains("_")) {
            checkbookItemDAO.save(item);
        }
    }


         ////////////////////////////////////////////////////////////////////
        //                                                                //
       //                Wechat checkbook download procedure             //
      //                                                                //
     //                                                                //
    ////////////////////////////////////////////////////////////////////

    private void checkWechat(final Date yesterday, final String account) {
        log.info("Begin to down load checkbook from Wechat for date [{}]", yesterday);

        String result = callWechat(yesterday, account);
        if (result == null) {
            log.warn("Wechat checkbook download failed!");
            return;
        }

        List<String> lines = Arrays.asList(result.split("\n"));
        lines.stream()
            .forEach(line -> {
                // Filter out the first line and the last line.
                if (line.length() <= 50 || line.contains("交易时间")) {
                    log.info(line);
                    return;
                }
                processWechatItem(line, statistic.get());
            });
       log.info(String.format("Totally [%s] transaction paid by Wechat at day [%s]", lines.size() - 3, yesterday));
    }

    private String callWechat(final Date yesterday, final String account) {
        Map<String, Object> parameters = new HashMap<>();
        String materialName = accountFactory.getMaterialNameByAccountName(account);
        parameters.put("appid", accountFactory.getPrivateKeyByMaterialName(materialName));
        /**
         * Generally, all the payment account related stuff should be stored in Payment Account service including the merchant id;
         * so the merchant_id should be retrieved from the payment account no.(originally from payment account service).
         * but for the back-end jobs, there is no chance calling the Payment Account service, so we have to store the merchant_id in Heimdallr
         * service for back-up(Heimdallr service is mainly responsible for managing the public and private key pairs).
         */
        parameters.put("mch_id", AccountUtils.get().getAdditional());
        parameters.put("device_info", "default");
        parameters.put("nonce_str", SignatureGenerator.MIXED_20.generate());
        parameters.put("bill_type", "ALL");
        parameters.put("bill_date", wechatFormat.format(yesterday));
        String sign = WechatSignatureHelper.getSignWithKey(parameters,
                accountFactory.getPublicKeyByMaterialName(accountFactory.getMaterialNameByAccountName(account)));
        parameters.put("sign", sign);
        Pair<String, String> pair = Pair.of(WechatConstants.DOWNLOAD_BILL_API, XMLUtils.mapToXmlStr(parameters));
        return wechatFacade.call(pair);
    }

    private void processWechatItem(final String line, final ConcurrentMap<String, AtomicDouble> statistic) {
        String[] values = line.split(",");
        String type = values[9].substring(1, values[9].length());
        CheckbookItem item = new CheckbookItem();
        String displayName = values[20].substring(1, values[20].length());
        BusinessProfile profile = BusinessProfile.fromShowName(displayName);

        if ("refund".equalsIgnoreCase(type)) {
            processWechatRefundItem(item, values, profile, statistic);
        } else {
            processWechatChargeItem(item, values, profile, statistic);
        }

        saveWechatCheckbookItem(item, profile, values);
    }

    private void processWechatRefundItem(final CheckbookItem item, final String[] values, final BusinessProfile profile,
            final ConcurrentMap<String, AtomicDouble> statistic) {
        // Refund item.
        // String business = null;
        String acknowledgedAmount = values[16].substring(1, values[16].length());
        item.setAcknowledgedAmount(acknowledgedAmount);
        item.setType(PaymentOperation.REFUND.operationType());
        String transactionID = values[15].substring(1, values[15].length());
        String externalTransactionID = values[14].substring(1, values[14].length());
        item.setTransactionID(transactionID);
        item.setExternalTransactionID(externalTransactionID);
        double fee = Double.parseDouble(acknowledgedAmount);
        //business = paymentRequestDAO.getPaymentRequestByTransactionID(transactionID).getBusiness();
        String statisticKey = StringUtils.join(new Object[] {profile.profile(),
                PaymentMethod.WECHAT.paymentMethodName(), PaymentOperation.REFUND.operationType()
            }, "_");
        AtomicDouble totalRefund = statistic.get(statisticKey);

        totalRefund.addAndGet(fee);
    }

    private void processWechatChargeItem(final CheckbookItem item, final String[] values, final BusinessProfile profile,
            final ConcurrentMap<String, AtomicDouble> statistic) {
        // Charge item.
        // String business = null;
        String externalTransactionID = values[5].substring(1, values[5].length());
        String transactionID = values[6].substring(1, values[6].length());
        item.setTransactionID(transactionID);

        String acknowledgedAmount = values[12].substring(1, values[12].length());
        item.setAcknowledgedAmount(acknowledgedAmount);

        item.setType(PaymentOperation.CHARGE.operationType());
        if (BusinessProfile.AUTO_VIP.equals(profile)) {
            item.setType(PaymentOperation.SCHEDULEDPAY.operationType());
        }
        double fee = Double.parseDouble(acknowledgedAmount);
        //business = paymentRequestDAO.getPaymentRequestByTransactionID(transactionID).getBusiness();
        String statisticKey = StringUtils.join(new Object[] {profile.profile(),
                PaymentMethod.WECHAT.paymentMethodName(), PaymentOperation.CHARGE.operationType()
            }, "_");
        AtomicDouble totalRefund = statistic.get(statisticKey);

        item.setExternalTransactionID(externalTransactionID);
        totalRefund.addAndGet(fee);
    }

    private void saveWechatCheckbookItem(final CheckbookItem item, final BusinessProfile profile, final String[] values) {
        item.setBusiness(profile.profile());
        item.setStatus(PaymentResponseStatus.SUCCESS.status());
        item.setRecordTime(Timestamp.valueOf(LocalDateTime.now()));
        item.setTransactionTime(Timestamp.valueOf(values[0].substring(1, values[0].length())));
        item.setPaymentMethod(PaymentMethod.WECHAT.paymentMethodName());
        item.setUniqueKey(values[5].substring(1, values[5].length()) + values[15].substring(1, values[15].length()));
        if (item.getTransactionID().length() == 32 && checkbookItemDAO.findByUniqueItem(item.getUniqueKey()) == null
                && !item.getTransactionID().contains("_")) {
            checkbookItemDAO.save(item);
        }
    }
}
