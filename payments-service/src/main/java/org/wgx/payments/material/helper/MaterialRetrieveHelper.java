package org.wgx.payments.material.helper;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Utility class to help retrieve useful data from the compressed material.
 * @author hzweiguanxiong
 *
 */
public final class MaterialRetrieveHelper {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    /**
     * Retrieve public key portion of the material.
     * @param material Compressed material.
     * @return Public key.
     * @throws IOException 
     * @throws JsonProcessingException 
     */
    public static String getPublicKeyAsString(final byte[] material)
            throws JsonProcessingException, IOException {
        String materialString = new String(material, "UTF-8");
        return MAPPER.readTree(materialString).get("publicKey").textValue();
    }

    /**
     * Retrive private key portion of the material.
     * @param material Compressed material.
     * @return Private key.
     * @throws JsonProcessingException
     * @throws IOException
     */
    public static String getPrivateKeyAsString(final byte[] material) 
            throws JsonProcessingException, IOException {
        String materialString = new String(material, "UTF-8");
        return MAPPER.readTree(materialString).get("privateKey").textValue();
    }

    /**
     * Retrive the additional portion of the material.
     * @param material Compressed material.
     * @return Additional data.
     * @throws JsonProcessingException
     * @throws IOException
     */
    public static String getAdditional(final byte[] material) 
            throws JsonProcessingException, IOException {
        String materialString = new String(material, "UTF-8");
        return MAPPER.readTree(materialString).get("additional").textValue();
    }
}
