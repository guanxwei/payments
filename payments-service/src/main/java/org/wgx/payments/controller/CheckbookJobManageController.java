package org.wgx.payments.controller;

import java.sql.Timestamp;
import java.util.Calendar;

import javax.annotation.Resource;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.wgx.payments.dao.FastSearchTableDAO;
import org.wgx.payments.model.FastSearchTableItem;
import org.wgx.payments.model.FastSearchTableItemStatus;
import org.wgx.payments.tools.JsonObject;

/**
 * SpringMVC controller to manage checkbook down job.
 *
 */
@RestController
@RequestMapping(path = "/api/music/payments/backfill/checkbook")
public class CheckbookJobManageController {

    @Resource(name = "fastSearchTableDAO")
    private FastSearchTableDAO fastSearchTableDAO;

    /**
     * Query single job entity.
     * @param date Date.
     * @return Job detail.
     */
    @RequestMapping(path = "/job/query")
    public JsonObject querySingle(@RequestParam(name = "date") final String date) {
        String key = "Job::" + date;
        FastSearchTableItem item = fastSearchTableDAO.find(key);
        if (item == null) {
            return JsonObject.start().code(500).msg("Job not exists");
        }
        return JsonObject.start().code(200).data(item);
    }

    /**
     * Retry the job.
     * @param date Job that needs to be retried.
     * @return Manipulation result.
     */
    @RequestMapping(path = "/job/retry")
    public JsonObject retry(final String date) {

        Calendar today = Calendar.getInstance();
        Calendar target = Calendar.getInstance();
        target.clear();
        try {
            target.set(Integer.parseInt(date.substring(0, 4)), Integer.parseInt(date.substring(4, 6)), Integer.parseInt(date.substring(6, 8)), 23, 59, 59);
        } catch (Exception e) {
            return JsonObject.start().code(400).msg("日期格式错误");
        }
        StringBuilder todayString = new StringBuilder();
        todayString.append(today.get(Calendar.YEAR)).append(today.get(Calendar.MONTH)).append(today.get(Calendar.DAY_OF_MONTH));
        if (todayString.toString().compareTo(date) <= 0) {
            return JsonObject.start().code(400).msg("请输入今天之前的日期");
        }
        FastSearchTableItem job = fastSearchTableDAO.find("Job::" + date);
        if (job == null || job.getStatus() != FastSearchTableItemStatus.PROCESSING.status()) {
            return JsonObject.start().code(404).msg("Job not available");
        }
        FastSearchTableItem item = new FastSearchTableItem();
        item.setItemKey("CheckbookDownlodRetry");
        item.setTransactionID(date);
        item.setStatus(FastSearchTableItemStatus.PROCESSING.status());
        item.setMessage("Retry failed checkbook download job");
        item.setTime(new Timestamp(System.currentTimeMillis()));
        fastSearchTableDAO.tryUpdateStatus(job.getStatus(), FastSearchTableItemStatus.RETRYING.status(), job.getId());
        fastSearchTableDAO.save(item);
        return JsonObject.start().code(200);
    }
}
