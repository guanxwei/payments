package org.wgx.payments.material.impl.dao;

import java.util.List;

import org.wgx.payments.material.impl.meta.Material;

/**
 * Material DAO.
 * @author hzweiguanxiong
 *
 */
public interface MaterialDAO {

    /**
     * Save a new material in DB.
     * @param material Material to be saved.
     * @return Manipulation result.
     */
    int save(final Material material);

    /**
     * Retrive a material from DB by name.
     * @param name Material's name.
     * @return Material entity.
     */
    Material retriveByName(final String name);

    /**
     * Get material list by user.
     * @param user User identity.
     * @return Material list.
     */
    List<Material> getListByUser(final String user);

    /**
     * Delete material.
     * @param name Material name.
     * @return Manipulation result.
     */
    long delete(final String name);
}
