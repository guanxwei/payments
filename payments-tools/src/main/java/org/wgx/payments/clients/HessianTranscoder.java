package org.wgx.payments.clients;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.caucho.hessian.io.HessianInput;
import com.caucho.hessian.io.HessianOutput;

import net.rubyeye.xmemcached.transcoders.SerializingTranscoder;
import net.rubyeye.xmemcached.transcoders.Transcoder;

/**
 * Hessian based implementation of {@linkplain Transcoder}.
 *
 */
public class HessianTranscoder extends SerializingTranscoder {
    private static final Logger LOG = LoggerFactory.getLogger(HessianTranscoder.class);

    @Override
    protected byte[] serialize(Object obj) {
        HessianOutput out = null;
        try {
            out = null;
            ByteArrayOutputStream byteArray = new ByteArrayOutputStream();
            out = new HessianOutput(byteArray);
            out.writeObject(obj);
            out.flush();
            return byteArray.toByteArray();
        } catch (Exception ex) {
            throw new IllegalArgumentException("Non-serializable object", ex);
        } finally {
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e) {
                    LOG.warn("close object output stream error!", e);
                }
            }
        }
    }

    @Override
    protected Object deserialize(byte[] data) {
        if (data == null) {
            return null;
        }
        HessianInput input = null;
        try {
            input = new HessianInput(new ByteArrayInputStream(data));
            return input.readObject();
        } catch (IOException e) {
            LOG.error("Caught IOException decoding " + data.length + " bytes of data", e);
            return null;
        } catch (Exception ex) {
            LOG.error("hessian2 deserialize error!", ex);
            return null;
        } finally {
            if (input != null) {
                input.close();
            }
        }
    }

}
