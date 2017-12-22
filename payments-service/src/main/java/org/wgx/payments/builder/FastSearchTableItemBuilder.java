package org.wgx.payments.builder;

import java.sql.Timestamp;

import org.wgx.payments.model.FastSearchTableItem;

/**
 * FastSearchTableItemBuilder.
 *
 */
public class FastSearchTableItemBuilder {

    // CHECKSTYLE:OFF
    private String itemKey;
    private String transactionID;
    private Timestamp time;
    private int status;
    private String message;

    public static FastSearchTableItemBuilder builder() {
        return new FastSearchTableItemBuilder();
    }

    public FastSearchTableItem build() {
        FastSearchTableItem item = new FastSearchTableItem();
        item.setItemKey(itemKey);
        item.setStatus(status);
        item.setTime(time);
        item.setTransactionID(transactionID);
        item.setMessage(message);
        return item;
    }

    public FastSearchTableItemBuilder itemKey(final String itemKey) {
        this.itemKey = itemKey;
        return this;
    }

    public FastSearchTableItemBuilder transactionID(final String transactionID) {
        this.transactionID = transactionID;
        return this;
    }

    public FastSearchTableItemBuilder time(final Timestamp time) {
        this.time = time;
        return this;
    }

    public FastSearchTableItemBuilder status(final int status) {
        this.status = status;
        return this;
    }

    public FastSearchTableItemBuilder message(final String message) {
        this.message = message;
        return this;
    }
    //CHECKSTYLE:ON
}
