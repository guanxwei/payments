package org.wgx.payments.facade;

import org.apache.commons.lang3.tuple.Pair;

import lombok.extern.slf4j.Slf4j;

/**
 * Alipay used facade to communicate with Alipay server.
 *
 */
@Slf4j
public class AlipayFacade implements Facade<Pair<String, String>, String> {

    /**
     * {@inheritDoc}
     */
    @Override
    public String call(final Pair<String, String> pair) {
        String url = pair.getLeft();
        try {
            return HttpClientManager.httpGet(url);
        } catch (Exception e) {
            log.warn("Failed to call Alipay server due to [{}]", e);
            return null;
        }
    }

}
