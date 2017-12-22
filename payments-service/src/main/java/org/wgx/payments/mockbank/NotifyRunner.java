package org.wgx.payments.mockbank;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.SortedMap;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.wgx.payments.facade.HttpClientManager;

import lombok.extern.slf4j.Slf4j;

/**
 * Mock bank worker to push notification to Payments Platform's
 * NotifyURLController.
 *
 */
@Slf4j
public class NotifyRunner implements Runnable {

    private String url;
    private SortedMap<String, String> content;

    /**
     * Constructor.
     *
     * @param url URL address.
     * @param content Content to be sent.
     */
    public NotifyRunner(final String url, final SortedMap<String, String> content) {
        this.url = url;
        this.content = content;
    }

    @Override
    public void run() {
        try {
            // Let the main thread have chances to update the request.
            Thread.sleep(500);
            List<NameValuePair> params = new ArrayList<>();
            for (Entry<String, String> entry : content.entrySet()) {
                params.add(new BasicNameValuePair(entry.getKey(), entry.getValue()));
            }

            String result = HttpClientManager.httpPost(url, params);
            log.info("Received processing result :" + result);
        } catch (Exception e) {
            log.warn("Fail to push back notification due to: ", e);
        }
    }

}
