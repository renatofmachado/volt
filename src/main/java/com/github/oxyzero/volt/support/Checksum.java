package com.github.oxyzero.volt.support;

import java.nio.charset.StandardCharsets;
import java.util.zip.CRC32;

public class Checksum {

    /**
     * Builds a checksum of the given string.
     *
     * @param string String
     * @return Checksum.
     */
    public static String make(String string)
    {
        CRC32 checksum = new CRC32();
        checksum.update(string.getBytes(StandardCharsets.UTF_8));

        return Long.toString(checksum.getValue());
    }

}
