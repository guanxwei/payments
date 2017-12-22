package org.wgx.payments.controller;

import java.util.List;

import javax.annotation.Resource;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.wgx.payments.dao.PaymentStatisticDAO;
import org.wgx.payments.model.PaymentStatistic;
import org.wgx.payments.tools.JsonObject;

/**
 * Back end controller to provide payment statistic data by date.
 * @author hzweiguanxiong
 *
 */
@RestController
public class StatisticController {

    @Resource(name = "paymentStatisticDAO")
    private PaymentStatisticDAO paymentStatisticDAO;

    /**
     * Get payment statistic record list by date.
     * @param begin Begin date.
     * @param end End date.
     * @return Payment statistic date
     */
    @RequestMapping(path = "/api/music/payments/backfill/statistic/list", method = {RequestMethod.GET})
    public JsonObject list(@RequestParam(name = "begin", required = true) final int begin,
            @RequestParam(name = "end", required = true) final int end) {
        List<PaymentStatistic> statistics = paymentStatisticDAO.getByDate(begin, end);
        return JsonObject.start().code(200).data(statistics);
    }
}
