package org.wgx.payments.controller;

import javax.annotation.Resource;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.wgx.payments.client.api.Service;
import org.wgx.payments.client.api.helper.PaymentOperation;
import org.wgx.payments.client.api.io.RefundRequest;
import org.wgx.payments.client.api.io.RefundResponse;
import org.wgx.payments.dao.PaymentResponseDAO;
import org.wgx.payments.model.PaymentResponse;
import org.wgx.payments.tools.JsonObject;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

/**
 * Backfill used controller to help refund to customers in case Payments plorform failed to
 * process refund request or duplicated charge requests.
 * @author hzweiguanxiong
 *
 */
@Slf4j
@RestController
@RequestMapping(path = "/api/music/payments/backfill/refund")
public class BackfillRefundController {

    @Resource(name = "paymentsRefundService")
    @Setter
    private Service<RefundRequest, RefundResponse> paymentsRefundService;

    @Resource(name = "paymentResponseDAO")
    private PaymentResponseDAO paymentResponseDAO;

    /**
     * Trigger refund manually for transaction.
     * @param transactionID Transaction id.
     * @param amount Refund amount.
     * @return Manipulation result.
     */
    @RequestMapping(path = "/single")
    public JsonObject refund(@RequestParam(name = "transactionID") final String transactionID,
            @RequestParam(name = "amount") final String amount) {
        log.info("Reiceve request to manully trigger refund for transaction [{}] with amount [{}]", transactionID, amount);
        PaymentResponse response = paymentResponseDAO.getPaymentResponseByTransactionID(transactionID);
        if (response == null) {
            return JsonObject.start().code(404).msg("Request not found");
        } else {
            RefundRequest refundRequest = new RefundRequest();
            refundRequest.setRefundAmount(amount);
            refundRequest.setTransactionID(transactionID);
            refundRequest.setCustomerID(response.getCustomerID());
            refundRequest.setPaymentOperationType(PaymentOperation.REFUND.operationType());
            //refundRequest.setReferenceID(response.getReferenceID());
            RefundResponse refundResponse = paymentsRefundService.execute(refundRequest);
            return JsonObject.start().code(refundResponse.getResponseCode()).msg(refundResponse.getResponseDescription());
        }
    }
}
