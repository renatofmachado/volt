package com.github.oxyzero.volt.middleware;

import com.github.oxyzero.volt.Request;
import com.github.oxyzero.volt.support.Container;
import com.github.oxyzero.volt.support.Encrypter;

/**
 * This middleware allows the decryption of a message before the action is executed.
 * 
 * @author Renato Machado
 */
public class MessageDecryptionMiddleware implements Middleware {
    
    /**
     * Encryption key.
     */
    private final String key;

    public MessageDecryptionMiddleware(String key) {
        if (key == null) {
            throw new IllegalArgumentException("The encryption key cannot be null.");
        }

        if (key.length() != 16) {
            throw new IllegalArgumentException("The encryption key must have 16 characters.");
        }
        
        this.key = key;
    }

    @Override
    public void before(Request request, Container container) {
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
    public void after(Request request, Container container) {}

}
