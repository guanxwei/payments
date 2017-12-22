package org.wgx.payments.material.impl.meta;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

/**
 * Plain entity to represent a user privilege detail record in DB.
 * @author hzweiguanxiong
 *
 */
@Data
@Builder
@AllArgsConstructor
public class PrivilegeRecord {

    public PrivilegeRecord() { }

    private long id;

    private String materialName;

    /**
     * Host's name, could be single host' name or the host-class's name.
     */
    private String host;

    private String user;

    /**
     * Privilege record type. 
     * 1 : Record for single one host
     * 2 : Record for a bundle of hosts within the same host-class.
     */
    private int type;
}
