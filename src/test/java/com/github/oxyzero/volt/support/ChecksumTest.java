package com.github.oxyzero.volt.support;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class ChecksumTest {

    public ChecksumTest() {}

    @Test
    public void test_can_create_instance() {
        Checksum c = new Checksum();
    }

    @Test
    public void test_checksum_works_as_expected() {
        String original = "a/b/c/";

        String expected = "1992621793";
        String result = Checksum.make(original);

        assertFalse(original.equals(result));
        assertTrue(result.equals(expected));

        String modified = "a/b/c";

        String modifiedExpected = "399835465";
        String modifiedResult = Checksum.make(modified);

        assertTrue(modifiedResult.equals(modifiedExpected));
        assertFalse(modifiedResult.equals(result));
        assertFalse(modifiedExpected.equals(expected));
        assertFalse(modified.equals(original));
    }
}
