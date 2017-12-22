package org.wgx.payments.utils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

import javax.crypto.Cipher;

/**
 * RSA utility to generate RSA based signature.
 *
 */
public final class RSAUtils {

    private static final String SIGN_ALGORITHMS = "SHA1WithRSA";

    private RSAUtils() { }

    /**
    * RSA sign.
    * @param content Source content.
    * @param privateKey Netease using private key.
    * @param charset Encoding set.
    * @return Signature.
    */
    public static String sign(final String content, final String privateKey, final String charset) {
        try {
            PKCS8EncodedKeySpec priPKCS8 = new PKCS8EncodedKeySpec(Base64.decode(privateKey));
            KeyFactory keyf = KeyFactory.getInstance("RSA");
            PrivateKey priKey = keyf.generatePrivate(priPKCS8);
            Signature signature = Signature.getInstance(SIGN_ALGORITHMS);
            signature.initSign(priKey);
            signature.update(content.getBytes(charset));
            byte[] signed = signature.sign();
            return Base64.encode(signed);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
    * RSA验签名检查.
    * @param content 待签名数据.
    * @param sign 签名值.
    * @param publicKey 支付宝公钥.
    * @param charset 编码格式.
    * @return 布尔值.
    */
    public static boolean verify(final String content, final String sign, final String publicKey, final String charset) {
        try {
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            byte[] encodedKey = Base64.decode(publicKey);
            PublicKey pubKey = keyFactory.generatePublic(new X509EncodedKeySpec(encodedKey));
            Signature signature = Signature.getInstance(SIGN_ALGORITHMS);
            signature.initVerify(pubKey);
            signature.update(content.getBytes(charset));
            return signature.verify(Base64.decode(sign));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
    * 解密.
    * @param content 密文.
    * @param privateKey 商户私钥.
    * @param charset 编码格式.
    * @return 解密后的字符串.
    * @throws Exception exception.
    */
    public static String decrypt(final String content, final String privateKey, final String charset) throws Exception {
        PrivateKey prikey = getPrivateKey(privateKey);
        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.DECRYPT_MODE, prikey);
        InputStream ins = new ByteArrayInputStream(Base64.decode(content));
        ByteArrayOutputStream writer = new ByteArrayOutputStream();
        byte[] buf = new byte[128];
        int bufl;
        while ((bufl = ins.read(buf)) != -1) {
            byte[] block = null;
            if (buf.length == bufl) {
                block = buf;
            } else {
                block = new byte[bufl];
                for (int i = 0; i < bufl; i++) {
                    block[i] = buf[i];
                }
            }
            writer.write(cipher.doFinal(block));
        }
        return new String(writer.toByteArray(), charset);
    }

    /**
    * 得到私钥.
    * @param key 密钥字符串（经过base64编码.
    * @throws Exception exception.
    * @return Private key.
    */
    public static PrivateKey getPrivateKey(final String key) throws Exception {
        byte[] keyBytes;
        keyBytes = Base64.decode(key);
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        PrivateKey privateKey = keyFactory.generatePrivate(keySpec);
        return privateKey;
    }
}
