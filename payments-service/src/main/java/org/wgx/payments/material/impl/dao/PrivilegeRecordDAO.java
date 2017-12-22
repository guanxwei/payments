package org.wgx.payments.material.impl.dao;

import java.util.List;

import org.wgx.payments.material.impl.meta.PrivilegeRecord;

/**
 * Privilege record DAO.
 * @author hzweiguanxiong
 *
 */
public interface PrivilegeRecordDAO {

    /**
     * Save a new privilege record in DB.
     * @param record Privilege record to be saved in DB.
     * @return Manipulation result.
     */
    int save(final PrivilegeRecord record);

    /**
     * Retrive privilege records onwd by this user.
     * @param user User identity.
     * @return Privilege record list owned by this user.
     */
    List<PrivilegeRecord> getListByUser(final String user);

    /**
     * Get privilege record by host and material name.
     * @param host Host's name
     * @param name Material's name.
     * @return Privilege entity,
     */
    PrivilegeRecord getByHostAndName(final String host, final String name);

}
