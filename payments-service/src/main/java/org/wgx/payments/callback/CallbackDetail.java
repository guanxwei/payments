package org.wgx.payments.callback;

import lombok.Data;

/**
 * Encapsulation of Callback result detail information.
 *
 */
@Data
public class CallbackDetail {

    /**
     * Flag indicates the callback result status.
     */
    private boolean succeed;

    /**
     * Error message returned only when callback fails.
     */
    private String error;

}
