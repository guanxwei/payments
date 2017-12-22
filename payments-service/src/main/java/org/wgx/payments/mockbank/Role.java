package org.wgx.payments.mockbank;

import javax.servlet.http.HttpServletRequest;

/**
 * Abstract of mock bank action role.
 *
 */
public interface Role {

    /**
     * The role name.
     * @return Role name.
     */
    String name();

    /**
     * Validate if the role support the incoming request.
     * @param request Request.
     * @return Result indicating if the role support the incoming request.
     */
    boolean validate(final HttpServletRequest request);

    /**
     * Role level. In some cases, there may be multi-roles supporting the incoming request.
     * To make sure the right role is selected, the role engine will pick up the role
     * with max level and execute on it.
     *
     * @return The role level.
     */
    int level();
}
