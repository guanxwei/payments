package org.wgx.payments.mockbank.wechat;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.Map;

import org.wgx.payments.utils.XMLUtils;

import lombok.extern.slf4j.Slf4j;

/**
 * Wechat mock bank used input stream handler.
 *
 */
@Slf4j
public final class InputStreamHanlder {

    private InputStreamHanlder() { }

    /**
     * Get parameter map from the input byte stream.
     * @param inputStream InputStream
     * @return Map<String, Object> instance.
     */
    public static Map<String, Object> handle(final InputStream inputStream) {
        try {
            ByteArrayOutputStream outSteam = new ByteArrayOutputStream();
            byte[] buffer = new byte[2048];
            int len;
            while ((len = inputStream.read(buffer)) != -1) {
                outSteam.write(buffer, 0, len);
            }
            outSteam.close();
            inputStream.close();
            String result  = new String(outSteam.toByteArray(), "UTF-8");
            log.info(String.format("Received XML response [%s] from Wechat", result));
            Map<String, Object> params = XMLUtils.getMapFromXML(result);
            return params;
        } catch (Exception e) {
            log.error("Fail to process XML response", e);
            return null;
        }
    }

}
