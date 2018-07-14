package com.boguan.passbox.utils;

import android.util.Log;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * Created by Lymons on 16/6/21.
 */

public class AES2 {
    private final String KEY_GENERATION_ALG = "PBKDF2WithHmacSHA1";
    private final int HASH_ITERATIONS = 10000;
    private final int KEY_LENGTH = 256;
    private byte[] salt = { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 0xA, 0xB, 0xC, 0xD, 0xE, 0xF };
    private PBEKeySpec myKeyspec;
    private final String CIPHERMODEPADDING = "AES/CBC/PKCS7Padding";
    private SecretKeyFactory keyfactory = null;
    private SecretKey sk = null;
    private SecretKeySpec skforAES = null;
    private byte[] iv = { 0xA, 1, 0xB, 5, 4, 0xF, 7, 9, 0x17, 3, 1, 6, 8, 0xC, 0xD, 91 };

    private IvParameterSpec IV;

    public AES2(String seed) {
        try {
            Charset cs = Charset.forName ("UTF-8");
            ByteBuffer bb = ByteBuffer.allocate (seed.getBytes().length);
            bb.put (seed.getBytes());
            bb.flip ();
            CharBuffer cb = cs.decode (bb);
            myKeyspec = new PBEKeySpec(cb.array(), salt, HASH_ITERATIONS, KEY_LENGTH);
            keyfactory = SecretKeyFactory.getInstance(KEY_GENERATION_ALG);
            sk = keyfactory.generateSecret(myKeyspec);
        } catch (NoSuchAlgorithmException nsae) {
            Log.e("P2PTransfer-AES", "no key factory support for PBEWITHSHAANDTWOFISH-CBC");
        } catch (InvalidKeySpecException ikse) {
            Log.e("P2PTransfer-AES", "invalid key spec for PBEWITHSHAANDTWOFISH-CBC");
        }
        byte[] skAsByteArray = sk.getEncoded();
        skforAES = new SecretKeySpec(skAsByteArray, "AES");
        IV = new IvParameterSpec(iv);
    }

    public String encrypt(byte[] plaintext) {
        byte[] ciphertext = encrypt(CIPHERMODEPADDING, skforAES, IV, plaintext);
        return Base64Encoder.encode(ciphertext);
    }

    public String decrypt(String ciphertext_base64) {
        byte[] s = Base64Decoder.decodeToBytes(ciphertext_base64);
        return new String(decrypt(CIPHERMODEPADDING, skforAES, IV, s));
    }

    private byte[] encrypt(String cmp, SecretKey sk, IvParameterSpec IV, byte[] msg) {
        try {
            Cipher c = Cipher.getInstance(cmp);
            c.init(Cipher.ENCRYPT_MODE, sk, IV);
            return c.doFinal(msg);
        } catch (NoSuchAlgorithmException nsae) {
            Log.e("P2PTransfer-AES", "no cipher getinstance support for " + cmp);
        } catch (NoSuchPaddingException nspe) {
            Log.e("P2PTransfer-AES", "no cipher getinstance support for padding " + cmp);
        } catch (InvalidKeyException e) {
            Log.e("P2PTransfer-AES", "invalid key exception");
        } catch (InvalidAlgorithmParameterException e) {
            Log.e("P2PTransfer-AES", "invalid algorithm parameter exception");
        } catch (IllegalBlockSizeException e) {
            Log.e("P2PTransfer-AES", "illegal block size exception");
        } catch (BadPaddingException e) {
            Log.e("P2PTransfer-AES", "bad padding exception");
        }
        return null;
    }

    private byte[] decrypt(String cmp, SecretKey sk, IvParameterSpec IV, byte[] ciphertext) {
        try {
            Cipher c = Cipher.getInstance(cmp);
            c.init(Cipher.DECRYPT_MODE, sk, IV);
            return c.doFinal(ciphertext);
        } catch (NoSuchAlgorithmException nsae) {
            Log.e("P2PTransfer-AES", "no cipher getinstance support for " + cmp);
        } catch (NoSuchPaddingException nspe) {
            Log.e("P2PTransfer-AES", "no cipher getinstance support for padding " + cmp);
        } catch (InvalidKeyException e) {
            Log.e("P2PTransfer-AES", "invalid key exception");
        } catch (InvalidAlgorithmParameterException e) {
            Log.e("P2PTransfer-AES", "invalid algorithm parameter exception");
        } catch (IllegalBlockSizeException e) {
            Log.e("P2PTransfer-AES", "illegal block size exception");
        } catch (BadPaddingException e) {
            Log.e("P2PTransfer-AES", "bad padding exception");
            e.printStackTrace();
        }
        return null;
    }
}
