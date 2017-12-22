package org.wgx.payments.controller;

import javax.annotation.Resource;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.wgx.payments.client.api.helper.PaymentResponseStatus;
import org.wgx.payments.dao.CheckOrderDiffDAO;
import org.wgx.payments.model.CheckOrderDiffItem;
import org.wgx.payments.model.CheckbookIssueItemStatus;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

/**
 * Refund callback controller.
 *
 */
@RestController
@Slf4j
public class RefundCallbackController {

    @Resource @Setter
    private CheckOrderDiffDAO checkOrderDiffDAO;

    /**
     * Backfill system's manually refund callback listener.
     * @param id Checkbook item id.
     * @param transactionID Payments-platform's refund transactionID.
     * @param status Refund status.
     * @return Processing result.
     */
    @RequestMapping(path = "/music/payments/backfill/refund/callback", method = RequestMethod.GET)
    public String callback(@PathVariable(value = "id") final long id,
            @PathVariable(value = "transactionID") final String transactionID, @PathVariable(value = "status") final int status) {
        CheckOrderDiffItem item = checkOrderDiffDAO.find(id);
        if (item == null) {
            log.warn("Checkbook item with id [{}] not exist, something wrong happened.", id);
            return "FAIL";
        } else {
            if (status != PaymentResponseStatus.SUCCESS.status()) {
                log.info("Manually refund did not succeed, should try again later.");
                checkOrderDiffDAO.updateStatus(id, CheckbookIssueItemStatus.FAIL.status());
            } else {
                checkOrderDiffDAO.updateStatus(id, CheckbookIssueItemStatus.REFUNDED.status());
            }
            return "SUCCESS";
        }
    }
}
