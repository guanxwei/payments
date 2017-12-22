package org.wgx.payments.callback;

import java.io.Serializable;

import org.wgx.payments.model.FastSearchTableItem;

import lombok.Data;

@Data
public class CallbackEvent implements Serializable {

    private static final long serialVersionUID = 3642756772566822311L;

    /**
     * Retry times.
     */
    private int times;

    /**
     * Json string representation of {@link FastSearchTableItem} instance.
     */
    private String detail;
}
