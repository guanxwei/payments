package org.wgx.payments.material.io;

import java.io.Serializable;

import lombok.Data;

/**
 * Encapsulation of RPC request to create a new material in Heimdallr service.
 * @author hzweiguanxiong
 *
 */
@Data
public class CreateMaterialRequest implements Serializable {

    /**
     * Auto generated version id.
     */
    private static final long serialVersionUID = 6034157263409189852L;

    /**
     * Material name.
     */
    private String name;

    /**
     * Material owner.
     */
    private String user;

    /**
     * Public key portion of the material.
     */
    private String publicKey;

    /**
     * Private key portioan of the material.
     */
    private String privateKey;

    /**
     * Additional property.
     */
    private String additional;
}
