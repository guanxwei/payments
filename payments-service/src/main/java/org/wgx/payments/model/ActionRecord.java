package org.wgx.payments.model;

import java.sql.Timestamp;

import lombok.Data;

/**
 * Action record. Mainly to record failure actions during working procedure, which will be helpful for
 * developers to locate potential bugs/issues.
 *
 */
@Data
public class ActionRecord {

    // CHECKSTYLE:OFF
    private long id;
    private String transactionID;
    private int errorCode;
    private String message;
    private Timestamp time;

    public enum ErrorCode {

        /**
         * Error code indicates callback failure.
         */
        CALLBACK_FAIL(0),

        /**
         * Error code indicates operation type not supported.
         */
        OPERATION_UNSUPPORT(1),

        /**
         * Error code indicates facade call failure.
         */
        FACADE_FAIL(2),

        /**
         * Error code indicates duplicated response.
         */
        DUPLICATE_RESPONSE(3),

        
        /**
         * Error code indicates payment request failure.
         */
        PAYMENT_FAIL(4),

        /**
         * Error code indicates payment response validation failure.
         */
        VALIDATION_FAIL(5),

        /**
         * Error code indicates checkbook download job failure.
         */
        CHECKBOOK_DOWNLOAD_FAIL(6);

        private int code;

        private ErrorCode(int code) {
            this.code = code;
        }

        public int code() {
            return this.code;
        }
    }
    // CHECKSTYLE:ON
}
