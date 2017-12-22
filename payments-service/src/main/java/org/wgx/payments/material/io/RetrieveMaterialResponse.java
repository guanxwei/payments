package org.wgx.payments.material.io;

import java.io.Serializable;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Encapsulation of RPC response returned when retrieving a material.
 * @author hzweiguanxiong
 *
 */
@Data
@EqualsAndHashCode(callSuper=false)
public class RetrieveMaterialResponse extends HeimdallrResponse implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = -1221943742515821317L;

    /**
     * Material.
     */
    private byte[] material;

}
