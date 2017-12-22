package org.wgx.payments.material.io;

import java.io.Serializable;

import lombok.Data;

/**
 * Encapsulation of RPC request to retrieve a material from Heimdallr service.
 * @author hzweiguanxiong
 *
 */
@Data
public class RetrieveMaterialRequest implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = -1742784590840340062L;

    /**
     * Material's name.
     */
    private String name;

    /**
     * Host the cilent run on.
     */
    private String host;

    /**
     * Host class.
     */
    private String hostClass;
}
