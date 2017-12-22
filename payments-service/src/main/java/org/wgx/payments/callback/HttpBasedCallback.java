package org.wgx.payments.callback;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Map;
import java.util.Map.Entry;

import org.wgx.payments.client.api.io.CallbackMetaInfo;
import org.wgx.payments.facade.HttpClientManager;
import org.wgx.payments.tools.Jackson;

import lombok.extern.slf4j.Slf4j;

/**
 * HTTP based callback implementation.
 *
 */
@Slf4j
public class HttpBasedCallback implements Callback {

    /**
     * {@inheritDoc}
     */
    @Override
    public CallbackDetail call(final CallbackMetaInfo info) {
        String message = "";
        CallbackDetail detail = new CallbackDetail();
        try {
            String queryString = buildQueryStringFromMap(info.getParameters());
            String url = info.getCallBackUrl() + "?" + queryString;
            log.info("Call [{}]", url);
            String result = HttpClientManager.httpGet(url);
            if ("SUCCESS".equals(result)) {
                detail.setSucceed(true);
            } else {
                detail.setError(result);
            }
        } catch (UnsupportedEncodingException e) {
            log.error("URL encode error happend to call " + Jackson.json(info) + " due to ", e);
            message = "URL encode error";
            detail.setError(message);
        } catch (IOException e) {
            log.error("Fail to call " + info.getCallBackUrl() + " due to ", e);
            message = "Network error.";
            detail.setError(message);
        }
        return detail;
    }

    private String buildQueryStringFromMap(final Map<String, String> map) throws UnsupportedEncodingException {
        StringBuilder sb = new StringBuilder();
        for (Entry<String, String> entry : map.entrySet()) {
            sb.append(entry.getKey()).append("=").append(URLEncoder.encode(entry.getValue(), "UTF-8")).append("&");
        }
        return sb.toString().substring(0, sb.length() - 1);
    }
}
