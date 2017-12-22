package org.wgx.payments.facade;

import org.apache.commons.lang3.tuple.Pair;
import org.wgx.payments.utils.IAPConstants;
import org.wgx.payments.utils.ThreadContext;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

/**
 * IAP facade.
 *
 */
@Slf4j
public class IAPFacade implements Facade<Pair<String, String>, Boolean> {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    /**
     * {@inheritDoc}
     */
    @Override
    public Boolean call(final Pair<String, String> pair) {
        String url = pair.getLeft();
        String receipt = pair.getRight();
        String result;
        int code = 100;
        try {
            result = HttpClientManager.httpPostBytes(url, IAPConstants.CHARSET, receipt.getBytes(IAPConstants.CHARSET));
            log.info("Apple validation result [{}]", result);
            code = Integer.parseInt(MAPPER.readTree(result).get("status").textValue());
            log.info("Response detail code is [{}]", code);
            ThreadContext.setMessage(Integer.toString(code));
        } catch (Exception e) {
            log.warn("Fail to connect to Apple's server", e);
            throw new RuntimeException();
        }
        return code == 0;
    }

}
