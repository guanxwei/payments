package org.wgx.payments.facade;

import org.apache.commons.lang3.tuple.Pair;

import lombok.extern.slf4j.Slf4j;

/**
 * Wechat facade to call Wechat to generate QR code link.
 *
 */
@Slf4j
public class WechatFacade implements Facade<Pair<String, String>, String> {

    @Override
    public String call(final Pair<String, String> pair) {
        String url = pair.getLeft();
        String entity = pair.getRight();
        log.info(String.format("Send request data [%s] to wechat server [%s]", entity, url));
        try {
            return HttpClientManager.httpPostXML(entity, url);
        } catch (Exception e) {
            log.warn("Fail to connect Wechat server", e);
            return null;
        }
    }

}
