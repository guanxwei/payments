package org.wgx.payments.mockbank;

import java.util.List;
import java.util.Optional;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import org.wgx.payments.deducer.Deducer;

import lombok.Setter;

/**
 * Role deducer.
 * @author hzweiguanxiong
 *
 */
public class RoleDeducer implements Deducer<HttpServletRequest, Role> {

    @Resource
    @Setter
    private List<Role> roles;

    /**
     * {@inheritDoc}
     */
    @Override
    public Role deduce(final HttpServletRequest request) {
        Optional<Role> targetRole;
        targetRole = roles.stream()
                .filter(role -> {
                    return role.validate(request);
                })
                .sorted((role1, role2) ->
                    role1.level() < role2.level() ? 1 : -1
                )
                .findFirst();

        if (!targetRole.isPresent()) {
            return null;
        } else {
            return targetRole.get();
        }
    }

}
