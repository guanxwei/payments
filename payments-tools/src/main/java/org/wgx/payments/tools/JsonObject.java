package org.wgx.payments.tools;

import java.util.HashMap;

/**
 * Generic HTTP API response
 *
 */

public class JsonObject extends HashMap<String, Object> {
    private static final long serialVersionUID = 8214023544330782528L;

    public static JsonObject start() {
        return new JsonObject();
    }

    public <T> JsonObject data(final T value) {
        this.put("data", value);
        return this;
    }

    public JsonObject code(final int code) {
        this.put("code", code);
        return this;
    }

    public JsonObject code(final String code) {
        this.put("code", code);
        return this;
    }


    public JsonObject msg(final String message) {
        this.put("msg", message);
        return this;
    }

    public JsonObject append(final String key, final Object value) {
        this.put(key, value);
        return this;
    }
}
