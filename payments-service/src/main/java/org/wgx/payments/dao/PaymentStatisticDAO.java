package org.wgx.payments.dao;

import java.util.List;

import org.wgx.payments.model.PaymentStatistic;

/**
 * PaymentStatisticDAO.
 * @author hzweiguanxiong
 *
 */
public interface PaymentStatisticDAO {

    /**
     * Save a statistic record in DB.
     * @param paymentStatistic Statistic detail info to be saved.
     * @return DB manipulation result.
     */
    public int save(final PaymentStatistic paymentStatistic);

    /**
     * Get the latest statistic records by business.
     * @param business Business.
     * @param date query date.
     * @return Latest PaymentStatistic list having business {@value business}
     */
    public List<PaymentStatistic> getLatestByBusiness(final String business, final int date);

    /**
     * Get the latest statistic records by business and payment method.
     * @param business Business.
     * @param date query date.
     * @param paymentMethod Payment method.
     * @return Latest PaymentStatistic list having business {@value business} and payment method {@value paymentMethod}
     */
    public List<PaymentStatistic> getLatestByBusinessAndPaymentMethod(final String business, final String paymentMethod,
            final int date);

    /**
     * Get payment statistic data within the range [being, end].
     * @param begin The begin date.
     * @param end The end date.
     * @return Payment statistic records.
     */
    public List<PaymentStatistic> getByDate(final int begin, final int end);
}
