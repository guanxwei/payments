package org.wgx.payments.model;

import java.io.Serializable;

import lombok.Data;

/**
 * Statistic table to record daily.
 * @author hzweiguanxiong
 *
 */
@Data
public class PaymentStatistic implements Serializable {

    /**
     * Auto generated version UID.
     */
    private static final long serialVersionUID = -7763761341955672760L;

    /**
     * DB identity.
     */
    private long id;

    /**
     * Business.
     */
    private String business;

    /**
     * Payment method.
     */
    private String paymentMethod;

    /**
     * Payment operation.
     */
    private String paymentOperation;

    /**
     * Statistic date with format "yyyyMMDD", for example "20170501".
     */
    private int date;

    /**
     * Total amount.
     */
    private String amount;

    /**
     * Statistic target.
     */
    private String target;
}
