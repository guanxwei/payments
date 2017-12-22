package org.wgx.payments.mockbank;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.servlet.http.HttpServletRequest;

/**
 * Abstract of action performed on the received requests. To make the mock bank work in
 * the same way for any processors, we should implement this interface to return mocking
 * behaviors of one 3P API.
 *
 */
public interface RoleAction {

    /**
     * Back end worker to push notification message.
     */
    ExecutorService EXECUTOR = Executors.newFixedThreadPool(5);

    /**
     * Return mocking result based on input.
     * @param request Http request.
     * @return Mocked result.
     */
    String act(final HttpServletRequest request);

}
