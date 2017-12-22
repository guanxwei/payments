package org.wgx.payments.tools;

import java.util.HashMap;

/**
 * Generic HTTP API response
 *
 */

public class JsonObject extends HashMap<String, Object>{
    private static final long serialVersionUID = 8214023544330782528L;

    public static JsonObject start(){
        return new JsonObject();
    }

    public <T> JsonObject data(T value){
        this.put("data", value);
        return this;
    }

    public JsonObject code(int code){
        this.put("code", code);
        return this;
    }

    public JsonObject code(String code){
        this.put("code", code);
        return this;
    }


    public JsonObject msg(String message){
        this.put("msg", message);
        return this;
    }

    public JsonObject append(String key, Object value){
        this.put(key, value);
        return this;
    }
}
