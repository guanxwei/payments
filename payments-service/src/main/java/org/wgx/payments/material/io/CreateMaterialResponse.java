package org.wgx.payments.material.io;

import java.io.Serializable;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Encapsulation of RPC response returned when creating new material.
 * @author hzweiguanxiong
 *
 */
@Data
@EqualsAndHashCode(callSuper=false)
public class CreateMaterialResponse extends HeimdallrResponse implements Serializable {

    /**
     * Auto generated version id.
     */
    private static final long serialVersionUID = -8162089604954357490L;

}
