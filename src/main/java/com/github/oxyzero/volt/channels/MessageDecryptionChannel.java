package com.github.oxyzero.volt.channels;

import com.github.oxyzero.volt.Request;
import com.github.oxyzero.volt.support.Encrypter;

import java.util.Map;

/**
 * This channel allows the decryption of a message before the action is executed.
 * 
 * @author Renato Machado
 */
public class MessageDecryptionChannel implements Channel {
    
    /**
     * Encryption key.
     */
    private final String key;

    public MessageDecryptionChannel(String key) {
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

            String decryptedMessage = encrypter.decrypt(message, key);

            request.message(decryptedMessage);
        } catch (Exception e) {
            // Ignore the exception and don't do anything with the message.
        }
    }

    @Override
    public void after(Request request, Map<String, Object> dependencies) {}

}
