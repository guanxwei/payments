package org.wgx.payments.signature;

import lombok.Data;

/**
 * Encapsulation of digital account registered in 3P payment gateway.
 *
 */
@Data
public class Account {

    private String accountName;

    private String materialName;

    private String publicKey;

    private String privateKey;

    private String additional;

    private String accountNo;
}
