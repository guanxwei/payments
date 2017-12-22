package org.wgx.payments.clients;

import java.util.List;
import java.util.Map;

/**
 * Memcache client.

 *
 */
public interface MemcacheClient {

    /**
     * Add the key to the memcache server if it not exists.
     * @param key Key
     * @param value Value
     * @return True if the key does not exist, otherwise false.
     */
    public boolean add(final String key, final Object value);

    /**
     * Add the key to the memcache server with specified expire time if the key does not exist.
     * @param key Key
     * @param value Value
     * @param expiry Living time.
     * @return True if the key does not exist, otherwise false.
     */
    public boolean add(final String key, final Object value, final int expiry);

    /**
     * Retrieve the object if the object exists.
     * @param key Object's key.
     * @return Cached object.
     */
    public Object get(final String key);

    /**
     * Retrieve a bundle of objects if the exist.
     * @param key Objects' keys.
     * @return Cached objects.
     */
    public Map<?,?> getMulti(final String[] keys);

    /**
     * Check if the key exists.
     * @param key Key to be checked.
     * @return
     */
    public boolean keyExists(final String key);

    /**
     * Remove the key from the memcache.
     * @param key Key to to removed.
     * @return True if the key is removed, otherwise false.
     */
    public boolean remove(final String key);

    /**
     * Remove the keys from the memcache.
     * @param keys Keys to to removed.
     * @return True if the key is removed, otherwise false.
     */
    public boolean remove(final List<String> keys);

    /**
     * Set the key's value as the provided object, not matter the key exists or not.
     * @param key Key.
     * @param value New value.
     * @return True if operation succeeds.
     */
    public boolean set(final String key, final Object value);

    /**
     * Set the key's value as the provided object with specified expire time, not matter the key exists or not.
     * @param key Key.
     * @param value New value.
     * @param _expiry Expire time.
     * @return True if operation succeeds.
     */
    public boolean set(final String key, final Object value, final int _expiry);
}
