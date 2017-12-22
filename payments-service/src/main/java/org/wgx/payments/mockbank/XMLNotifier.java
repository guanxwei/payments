package org.wgx.payments.mockbank;

import org.wgx.payments.facade.HttpClientManager;

import lombok.extern.slf4j.Slf4j;

/**
 * Back end notifier to send back XML string.
 *
 */
@Slf4j
public class XMLNotifier implements Runnable {

    private String url;
    private String xml;

    /**
     * Constructor.
     * @param url URL.
     * @param xml XML.
     */
    public XMLNotifier(final String url, final String xml) {
        this.url = url;
        this.xml = xml;
    }

    @Override
    public void run() {
        try {
            // Let the main thread have chances to update the request.
            Thread.sleep(500);

            String result = HttpClientManager.httpPostXML(xml, url);
            log.info("Received processing result :" + result);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
