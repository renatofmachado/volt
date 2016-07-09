package com.github.oxyzero.volt.channels;

import com.github.oxyzero.volt.Request;
import com.github.oxyzero.volt.support.Encrypter;

import java.util.Map;

/**
 * This channel allows the encryption of a message before the action is executed.
 * 
 * @author Renato Machado
 */
public class MessageEncryptionChannel implements Channel {
    
    /**
     * Encryption key.
     */
    private final String key;
    
    public MessageEncryptionChannel(String key)
    {
        if (key == null) {
            throw new IllegalArgumentException("The encryption key cannot be null.");
        }
        
        if (key.length() != 16) {
            throw new IllegalArgumentException("The encryption key must have 16 characters.");
        }
        
        this.key = key;
    }
    
    @Override
    public void before(Request request, Map<String, Object> dependencies) {
        String message = request.message();
        
        try {
            Encrypter encrypter = new Encrypter();

            String encryptedMessage = encrypter.encrypt(message, key);

            request.message(encryptedMessage);
        } catch (Exception e) {
            // Ignore the exception and don't do anything with the message.
        }
    }

    @Override
    public void after(Request request, Map<String, Object> dependencies) {}

}
