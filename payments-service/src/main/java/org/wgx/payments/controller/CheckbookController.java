package org.wgx.payments.controller;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.annotation.Resource;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.wgx.payments.builder.ActionRecordBuilder;
import org.wgx.payments.builder.PaymentResponseBuilder;
import org.wgx.payments.client.api.Service;
import org.wgx.payments.client.api.helper.PaymentOperation;
import org.wgx.payments.client.api.helper.PaymentResponseStatus;
import org.wgx.payments.client.api.helper.ResponseStatus;
import org.wgx.payments.client.api.io.CallbackMetaInfo;
import org.wgx.payments.client.api.io.RefundRequest;
import org.wgx.payments.client.api.io.RefundResponse;
import org.wgx.payments.dao.ActionRecordDAO;
import org.wgx.payments.dao.CheckOrderDiffDAO;
import org.wgx.payments.dao.CheckbookItemDAO;
import org.wgx.payments.dao.PaymentRequestDAO;
import org.wgx.payments.dao.PaymentResponseDAO;
import org.wgx.payments.dao.service.PaymentsDAOService;
import org.wgx.payments.model.ActionRecord;
import org.wgx.payments.model.CheckOrderDiffItem;
import org.wgx.payments.model.CheckbookIssueItemStatus;
import org.wgx.payments.model.CheckbookItem;
import org.wgx.payments.model.PaymentRequest;
import org.wgx.payments.model.PaymentRequestStatus;
import org.wgx.payments.model.PaymentResponse;
import org.wgx.payments.tools.Jackson;
import org.wgx.payments.tools.JsonObject;
import org.wgx.payments.utils.UserContext;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

/**
 * Checkbook controller.
 *
 */
@Slf4j
@RestController
@RequestMapping(path = "/api/music/payments/backfill/checkbook")
public class CheckbookController {

    @Resource(name = "checkbookItemDAO")
    private CheckbookItemDAO checkbookItemDAO;

    @Resource @Setter
    private PaymentRequestDAO paymentRequestDAO;

    @Resource @Setter
    private PaymentResponseDAO paymentResponseDAO;

    @Resource @Setter
    private PaymentsDAOService paymentsDAOService;

    @Resource @Setter
    private ActionRecordDAO actionRecordDAO;

    @Resource(name = "paymentsRefundService")
    @Setter
    private Service<RefundRequest, RefundResponse> paymentsRefundService;

    @Value(value = "${refund.callback}")
    private String refundCallback;

    @Resource @Setter
    private CheckOrderDiffDAO checkOrderDiffDAO;

    /**
     * Default status to be used to get checkbook item lists.
     * Checkbook status enumeration please refer to {@link CheckbookIssueItemStatus}
     */
    private static final String DEFAULT_STATUS = "3";

    /**
     * Get checkbook item list from the DB based on the input query condition.
     * @param limit Item numeric limit.
     * @param offset The offset of the first item.
     * @param status Item status.
     * @return Checkbook item list.
     */
    @Deprecated
    public String list(@RequestParam(name = "limit") final int limit,
            @RequestParam(name = "offset") final int offset,
            @RequestParam(name = "status", defaultValue = DEFAULT_STATUS) final int status) {
        log.info("Checkbook query condition, status : [{}], offset : [{}], limit : [{}]", status, offset, limit);
        List<CheckbookItem> items = checkbookItemDAO.list(limit, offset, status);
        return Jackson.json(items);
    }

    /**
     * Get checkbook issue item list from the DB based on the input query condition.
     * @param page Page no.
     * @param status Diff item status.
     * @return Checkbook item list.
     */
    @RequestMapping(path = "/issues/list", method = {RequestMethod.GET})
    public JsonObject issues(@RequestParam(name = "page") final int page,
            @RequestParam(name = "status", defaultValue = DEFAULT_STATUS) final int status) {
        log.info("Checkbook issue query condition, status : [{}], page : [{}], limit : [{}]", status, page, 20);
        int offset = (page - 1) * 20;
        List<CheckOrderDiffItem> items = checkOrderDiffDAO.list(20, offset, status);
        return JsonObject.start().code(200).data(items).append("count", checkOrderDiffDAO.count(status));
    }

    /**
     * Manually tackle error checkbook item, simply update the item status.
     * @param id Checkbook item's id.
     * @param status Target status.
     * @return DB manipulation result.
     */
    @RequestMapping(path = "/issue/process", method = {RequestMethod.GET})
    public JsonObject process(@RequestParam(name = "id") final long id,
            @RequestParam(name = "status") final int status) {
        if (checkOrderDiffDAO.updateStatus(id, status) == 1) {
            ActionRecord record = ActionRecordBuilder.builder()
                    .message(UserContext.getUser() + "忽略异常订单:" + id)
                    .time(new Timestamp(System.currentTimeMillis()))
                    .transactionID(String.valueOf(id))
                    .errorCode(0)
                    .build();
            actionRecordDAO.record(record);
            return JsonObject.start().code(200);
        } else {
            return JsonObject.start().code(500).msg("Processing error！");
        }
    }

    /**
     * Trigger manually refund to 3P payment gateway.
     *
     * This API is used for cases:
     * (1) Payments-platform only has one pending on paid payment request record, while the 3P payment gateway shows it has been paid successfully.
     * (2) Payments-platform has both one paid payment request and one failed payment response,
     *      while the 3P payment gateway shows it has been paid successfully.
     * (3) Manually refund failed, should try again.
     * @param id Checkbook item id.
     * @return Action result.
     */
    @RequestMapping(path = "/item/refund")
    public JsonObject trigerRefund(@RequestParam(name = "id") final long id) {
        log.info("Manually refund for checkbook item [{}]", id);
        try {
            CheckOrderDiffItem item = checkOrderDiffDAO.find(id);
            if (item == null) {
                log.warn("Unvalid checkbook item id [{}]. Something wrong happened", id);
                return JsonObject.start().code(404).msg("Item not exists");
            } else {
                String transactionID = item.getTransactionID();
                PaymentRequest request = paymentRequestDAO.getPaymentRequestByTransactionID(transactionID);
                PaymentResponse response = paymentResponseDAO.getPaymentResponseByTransactionID(transactionID);
                if (request == null) {
                    log.warn("Payment request not found. Something wrong happened");
                    return JsonObject.start().code(404).msg("Request not exists");
                } else {
                    Timestamp now = Timestamp.valueOf(LocalDateTime.now());
                    request.setStatus(PaymentRequestStatus.PAID.status());
                    request.setLastUpdateTime(now);
                    if (response == null) {
                        String externalTransactionID = null;
                        List<CheckbookItem> items = checkbookItemDAO.getListByTransactionID(transactionID);
                        Optional<CheckbookItem> optionalItem = items.stream()
                                .filter(bookItem -> {
                                    return bookItem.getType().equals(PaymentOperation.CHARGE.operationType());
                                })
                                .findFirst();
                        if (optionalItem.isPresent()) {
                            externalTransactionID = optionalItem.get().getExternalTransactionID();
                        }
                        if (externalTransactionID == null) {
                            log.warn("External transaction id is not available");
                            return JsonObject.start().code(404).msg("External transaction id not available");
                        }
                        response = PaymentResponseBuilder.builder()
                                .acknowledgedAmount(item.getAcknowledgedAmount())
                                .business(request.getBusiness())
                                .createTime(now)
                                .customerID(request.getCustomerID())
                                .externalTransactionID(externalTransactionID)
                                .lastUpdateTime(now)
                                .operationType(request.getPaymentOperationType())
                                .paymentMethod(request.getPaymentMethod())
                                .rawResponse("Backfill system")
                                .referenceID(request.getReferenceID())
                                .status(PaymentResponseStatus.SUCCESS.status())
                                .transactionID(transactionID)
                                .build();
                        paymentResponseDAO.save(response);
                    } else {
                        response.setStatus(PaymentResponseStatus.SUCCESS.status());
                        response.setLastUpdateTime(now);
                    }
                    boolean result = paymentsDAOService.updateRequestAndResponse(request, response);
                    if (!result) {
                        log.warn("Fail to update related payment request and response record.");
                        return JsonObject.start().code(500).msg("DB error");
                    }
                    RefundRequest refundRequest = new RefundRequest();
                    refundRequest.setBusiness(request.getBusiness());
                    CallbackMetaInfo callback = new CallbackMetaInfo();
                    Map<String, String> parameters = new HashMap<>();
                    parameters.put("id", String.valueOf(item.getId()));
                    callback.setCallBackUrl(refundCallback);
                    refundRequest.setCallbackInfo(callback);
                    refundRequest.setCustomerID(request.getCustomerID());
                    refundRequest.setPaymentOperationType(PaymentOperation.REFUND.operationType());
                    refundRequest.setReferenceID(request.getReferenceID());
                    refundRequest.setRefundAmount(item.getAcknowledgedAmount());
                    refundRequest.setTransactionID(item.getTransactionID());
                    Map<String, String> specialTags = new HashMap<>();
                    specialTags.put("channel", "backfill");
                    RefundResponse refundResponse = paymentsRefundService.execute(refundRequest);
                    if (refundResponse.getResponseCode() == ResponseStatus.INTERNAL_ERROR_CODE) {
                        log.warn("Fail to trigger manually refund");
                        return JsonObject.start().code(500).msg("Item not exists");
                    }
                    checkOrderDiffDAO.updateStatus(id, CheckbookIssueItemStatus.REFUNDING.status());
                    ActionRecord record = ActionRecordBuilder.builder()
                            .message(UserContext.getUser() + "手动触发退款请求:" + item.getId())
                            .transactionID(item.getTransactionID())
                            .time(new Timestamp(System.currentTimeMillis()))
                            .build();
                    actionRecordDAO.record(record);
                }
                return JsonObject.start().code(200);
            }
        } catch (Exception e) {
            return JsonObject.start().code(500).msg(e.getMessage());
        }
    }

    /**
     * Get checkbook item list via transaction ID.
     * @param transactionID Payment transaction ID.
     * @return Checkbook item list.
     */
    @RequestMapping(path = "/items/list", method = {RequestMethod.GET})
    public String getCheckbookItemList(@RequestParam(name = "transactionID") final String transactionID) {
        Map<String, Object> items = new HashMap<>();
        List<CheckbookItem> list = checkbookItemDAO.getListByTransactionID(transactionID);
        items.put("code", 200);
        items.put("data", list);
        return Jackson.json(items);
    }

    /**
     * Get the check book related view info.
     * @param id {@linkplain CheckOrderDiffItem} id.
     * @return CheckOrderDiffItem related information.
     */
    @RequestMapping(path = "/issue/view", method = {RequestMethod.GET})
    public JsonObject view(final long id) {
        CheckOrderDiffItem diff = checkOrderDiffDAO.find(id);
        if (diff == null || diff.getStatus() != CheckbookIssueItemStatus.FAIL.status()) {
            log.info("Try to query unexisted checkbook diff item [{}]", id);
            return JsonObject.start().code(404).msg("Checkbook not exists!");
        }

        PaymentRequest paymentRequest = paymentRequestDAO.getPaymentRequestByTransactionID(diff.getTransactionID());
        if (paymentRequest == null) {
            log.info("Try to query unexisted checkbook diff item [{}]", id);
            return JsonObject.start().code(404).msg("Checkbook not exists!");
        }
        PaymentResponse paymentResponse = paymentResponseDAO.getPaymentResponseByTransactionID(diff.getTransactionID());
        List<CheckbookItem> items = checkbookItemDAO.getListByTransactionID(diff.getTransactionID());
        int canAutoRefunded = 0;
        if (paymentResponse == null && items.size() > 0
                && paymentRequest.getPaymentOperationType().equals(PaymentOperation.CHARGE.operationType())) {
            canAutoRefunded = 1;
        } else if (paymentResponse != null && paymentResponse.getStatus() == PaymentResponseStatus.FAIL.status()
                && items.size() > 0 && paymentRequest.getPaymentOperationType().equals(PaymentOperation.CHARGE.operationType())) {
            canAutoRefunded = 1;
        }
        return JsonObject.start()
                .code(200)
                .append("request", Jackson.json(paymentRequest))
                .append("response", Jackson.json(paymentResponse))
                .append("items", Jackson.json(items))
                .append("canAutoRefunded", canAutoRefunded);
    }
}
