package org.wgx.payments.material.impl.meta;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

/**
 * Plain entity to represent a "material" in DB.
 * @author hzweiguanxiong
 *
 */
@Data
@Builder
@AllArgsConstructor
public class Material {

    public Material() { }

    private long id;

    private String name;

    private String user;

    private String publicKey;

    private String privateKey;

    private String additional;
}
