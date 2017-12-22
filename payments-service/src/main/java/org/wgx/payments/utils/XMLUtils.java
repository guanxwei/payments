package org.wgx.payments.utils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;

/**
 * Wechat used XML utility.
 */
public final class XMLUtils {

    private XMLUtils() { }

    /**
     * Retrieve map from XML style string.
     * @param xmlString Input XML style string.
     * @return Map.
     * @throws Exception Exception
     */
    public static Map<String, Object> getMapFromXML(final String xmlString) throws Exception {

        Document document = DocumentHelper.parseText(xmlString);
        Element root = document.getRootElement();
        @SuppressWarnings("unchecked")
        List<Element> elements = root.elements();
        Map<String, Object> map = new HashMap<>();
        for (Element element : elements) {
            map.put(element.getName(), element.getText());
        }
        return map;
    }

    /**
     * Generate XML style string from input map.
     * @param reqMap Input map containing information.
     * @return XML style string.
     */
    public static String mapToXmlStr(final Map<String, Object> reqMap) {
        StringBuilder sb = new StringBuilder();
        sb.append("<xml>");
        for (Map.Entry<String, Object> entry : reqMap.entrySet()) {
            sb.append("<").append(entry.getKey()).append(">")
                .append("<![CDATA[").append(entry.getValue())
                .append("]]>").append("</").append(entry.getKey()).append(">");
        }
        sb.append("</xml>");
        return sb.toString();
    }
}

