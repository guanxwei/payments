package org.wgx.payments.mockbank.callback;

import javax.servlet.http.HttpServletRequest;

import org.wgx.payments.mockbank.RoleAction;

import lombok.extern.slf4j.Slf4j;

/**
 * Alipay charge action mocker.
 *
 */
@Slf4j
public class CallbackAction implements RoleAction {

    /**
     * {@inheritDoc}
     */
    @Override
    public String act(final HttpServletRequest request) {
        log.info("Received request for callback :" + request.getRequestURL());
        return "success";
    }
}
