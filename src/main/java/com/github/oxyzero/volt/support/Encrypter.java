package com.github.oxyzero.volt.support;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;
import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

/**
 * This class allows the encryption and decryption of messages using the AES algorithm.
 * 
 * @author Renato Machado
 */
public class Encrypter {
    
    /**
     * Encrypts a given message with the given key.
     * 
     * @param message Message.
     * @param key Key.
     * @return Encrypted message.
     */
    public String encrypt(String message, byte[] key)
    {
        if (message == null || message.isEmpty()) {
            throw new IllegalArgumentException("Invalid message was given. A message cannot be null or empty.");
        }
        
        try {
            Cipher cipher = Cipher.getInstance("AES");
            SecretKeySpec keySpec = new SecretKeySpec(key, "AES");
            
            cipher.init(Cipher.ENCRYPT_MODE, keySpec);
            
            byte[] byteDataToEncrypt = message.getBytes();
            byte[] byteCipherText = cipher.doFinal(byteDataToEncrypt);
            
            return new String(Base64.getEncoder().encode(byteCipherText));
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | IllegalBlockSizeException | BadPaddingException ex) {
            throw new IllegalArgumentException(ex.getMessage());
        }
    }
    
    /**
     * Encrypts a given message with the given key.
     *
     * @param message Message.
     * @param key Key.
     * @return Encrypted message.
     */
    public String encrypt(String message, String key)
    {
        try {
            byte[] keyInBytes = key.getBytes("UTF-8");
            
            return this.encrypt(message, keyInBytes);
        } catch (UnsupportedEncodingException ex) {
            throw new IllegalArgumentException(ex.getMessage());
        }
    }
    
    /**
     * Decrypts a given message with the given key.
     * 
     * @param message Message.
     * @param key Key.
     * @return Decrypted message.
     */
    public String decrypt(String message, byte[] key)
    {
        try {
            byte[] data = Base64.getDecoder().decode(message);
            SecretKeySpec keySpec = new SecretKeySpec(key, "AES");
            
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.DECRYPT_MODE, keySpec);
            
            byte[] plainData = cipher.doFinal(data);
            
            return new String(plainData);
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | IllegalBlockSizeException | BadPaddingException ex) {
            throw new IllegalArgumentException(ex.getMessage());
        }
    }
    
    /**
     * Decrypts a given message with the given key.
     *
     * @param message Message.
     * @param key Key.
     * @return Decrypted message.
     */
    public String decrypt(String message, String key) {
        try {
            byte[] keyInBytes = key.getBytes("UTF-8");

            return this.decrypt(message, keyInBytes);
        } catch (UnsupportedEncodingException ex) {
            throw new IllegalArgumentException(ex.getMessage());
        }
    }
    
}
