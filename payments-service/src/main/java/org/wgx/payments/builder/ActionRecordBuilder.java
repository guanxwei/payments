package org.wgx.payments.builder;

import java.sql.Timestamp;

import org.wgx.payments.model.ActionRecord;

/**
 * ActionRecordBuilder.
 *
 */
public class ActionRecordBuilder {

    // CHECKSTYLE:OFF
    private String transactionID;
    private int errorCode;
    private String message;
    private Timestamp time;
    private long id;

    public ActionRecord build() {
        ActionRecord record = new ActionRecord();
        record.setMessage(message);
        record.setErrorCode(errorCode);
        record.setTime(time);
        record.setTransactionID(transactionID);
        record.setId(id);
        return record;
    }

    public static ActionRecordBuilder builder() {
        return new ActionRecordBuilder();
    }

    public ActionRecordBuilder transactionID(final String transactionID) {
        this.transactionID = transactionID;
        return this;
    }

    public ActionRecordBuilder errorCode(final int errorCode) {
        this.errorCode = errorCode;
        return this;
    }

    public ActionRecordBuilder message(final String message) {
        this.message = message;
        return this;
    }

    public ActionRecordBuilder time(final Timestamp time) {
        this.time = time;
        return this;
    }

    public ActionRecordBuilder id(final long id) {
        this.id = id;
        return this;
    }
    // CHECKSTYLE:ON
}
