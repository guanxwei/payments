package org.wgx.payments.controller;

import java.util.LinkedList;
import java.util.List;

import javax.annotation.Resource;

import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.wgx.payments.client.api.helper.ResponseStatus;
import org.wgx.payments.dao.FastSearchTableDAO;
import org.wgx.payments.dao.PaymentRequestDAO;
import org.wgx.payments.dao.PaymentResponseDAO;
import org.wgx.payments.model.PaymentRequest;
import org.wgx.payments.model.PaymentResponse;
import org.wgx.payments.tools.Jackson;

import lombok.extern.slf4j.Slf4j;

/**
 * Payment transaction information manage controller for backfill system.
 *
 */
@RestController
@Slf4j
public class PaymentTransactionManageController {

    @Resource
    private PaymentRequestDAO paymentRequestDAO;

    @Resource
    private FastSearchTableDAO fastSearchTableDAO;

    @Resource
    private PaymentResponseDAO paymentResponseDAO;

    /**
     * Get payment transaction information from the DB.
     * @param business Business the reference belongs to.
     * @param referenceID The reference ID.
     * @param map SpringMVC model map instance.
     * @return Jsonfied map information contains payment transaction information.
     */
    @RequestMapping(path = "/api/music/payments/backfill/transaction/info/id", method = {RequestMethod.GET})
    public String getPaymentTransactionInformation(@RequestParam(name = "business", required = true) final String business,
            @RequestParam(name = "referenceID") final String referenceID, final ModelMap map) {
        //FastSearchTableItem item = fastSearchTableDAO.find("charge" + referenceID + business);
        List<PaymentRequest> requests;
        List<PaymentResponse> responses = new LinkedList<>();
        log.info("Backfill system can not find related fast search item, will try to find related payment request directly "
                + "from the Payment Request table.");
        requests = paymentRequestDAO.getPaymentRequestByReferenceIDAndBusinessName(referenceID, business);
        if (requests == null || requests.isEmpty()) {
            log.info("Can not find any record related to the input referenceID [{}] and business [{}]", referenceID, business);
            map.put("code", ResponseStatus.INTERNAL_ERROR_CODE);
            map.put("message", "Record missing");
        } else {
            for (PaymentRequest request : requests) {
                PaymentResponse response = paymentResponseDAO.getPaymentResponseByTransactionID(request.getTransactionID());
                if (response != null) {
                    responses.add(response);
                }
            }
            map.put("requests", Jackson.json(requests));
            map.put("responses", Jackson.json(responses));
            map.put("code", ResponseStatus.PROCESS_SUCCESS_CODE);
        }
        return Jackson.json(map);
    }
}
