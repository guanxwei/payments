package org.wgx.payments.clients;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.wgx.payments.exception.MemcacheException;
import org.wgx.payments.transaction.WriteableAction;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.rubyeye.xmemcached.MemcachedClient;
import net.rubyeye.xmemcached.MemcachedClientBuilder;
import net.rubyeye.xmemcached.XMemcachedClientBuilder;
import net.rubyeye.xmemcached.impl.KetamaMemcachedSessionLocator;

/**
 * Xmemcached based implementation of {@linkplain MemcacheClient}.
 *
 */
@Slf4j
public class MemcacheClientImpl implements MemcacheClient {

    private static final int DEFAULT_EXPIRE_TIME = 3600 * 24 * 30;
    private MemcachedClientBuilder builder;
    private MemcachedClient client;

    @Setter
    private String servers;

    @Setter
    private int poolSize = 5;

    public void init() throws IOException {
        if (servers == null) {
            throw new MemcacheException("Memcache servers must be specified");
        }
        builder = new XMemcachedClientBuilder(servers.replace(",", " "));
        builder.setConnectionPoolSize(2);
        builder.setConnectTimeout(3000);
        builder.setSessionLocator(new KetamaMemcachedSessionLocator());
        builder.setTranscoder(new HessianTranscoder());
        client = builder.build();
    }

    /**
     * {@inheritDoc}
     */
    @WriteableAction
    @Override
    public boolean add(final String key, final Object value) {
        try {
            return client.add(key, DEFAULT_EXPIRE_TIME, value);
        } catch (Exception e) {
            log.error("Memcache add error", e);
            return false;
        }
    }

    /**
     * {@inheritDoc}
     */
    @WriteableAction
    @Override
    public boolean add(final String key, final Object value, final int expiry) {
        try {
            return client.add(key, expiry, value);
        } catch (Exception e) {
            log.error("Memcache expire add error", e);
            return false;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object get(final String key) {
        try {
            return client.get(key);
        } catch (Exception e) {
            log.error("Memcache get error", e);
            return null;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Map<?, ?> getMulti(final String[] keys) {
        try {
            List<String> keyList = Arrays.asList(keys);
            Map<?, ?> result = new HashMap<>();
            int count = keyList.size() / 50;
            for (int i = 0; i <= count; i++) {
                int begin = i * 50;
                int end = begin + 50;
                getMulti(result, keyList.subList(begin, end > keyList.size() ? keyList.size() : end));
            }
            return result;
        } catch (Exception e) {
            log.error("Memcache multi get error", e);
            return null;
        }
    }

    /**
     * {@inheritDoc}
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    private void getMulti(final Map map, final List<String> keyList) {
        for (int count = 2; count > 0; count--) {
            try {
                Map<?, ?> values = client.get(keyList);
                map.putAll(values);
            } catch (Exception e) {
                log.error("Memcache multi get failed error", e);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean keyExists(final String key) {
        try {
            return client.get(key) != null;
        } catch (Exception e) {
            log.error("Memcache key check error", e);
            return false;
        }
    }

    /**
     * {@inheritDoc}
     */
    @WriteableAction
    @Override
    public boolean remove(final String key) {
        try {
            return client.delete(key);
        } catch (Exception e) {
            log.error("Memcache key check error", e);
            return false;
        }
    }

    /**
     * {@inheritDoc}
     */
    @WriteableAction
    @Override
    public boolean set(final String key, final Object value) {
        try {
            return client.set(key, DEFAULT_EXPIRE_TIME, value);
        } catch (Exception e) {
            log.error("Memcache set error", e);
            return false;
        }
    }

    /**
     * {@inheritDoc}
     */
    @WriteableAction
    @Override
    public boolean set(final String key, final Object value, final int _expiry) {
        try {
            return client.set(key, _expiry, value);
        } catch (Exception e) {
            log.error("Memcache expire set error", e);
            return false;
        }
    }

    /**
     * {@inheritDoc}
     */
    @WriteableAction
    @Override
    public boolean remove(final List<String> keys) {
        try {
            for (int i = 0; i < keys.size(); i++) {
                client.delete(keys.get(i));
            }
            return true;
        } catch (Exception e) {
            log.error("Memcache key check error", e);
            return false;
        }
    }

}
