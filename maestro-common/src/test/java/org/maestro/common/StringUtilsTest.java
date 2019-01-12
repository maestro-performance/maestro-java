package org.maestro.common;

import org.junit.Test;

import static org.junit.Assert.*;

public class StringUtilsTest {

    @Test
    public void testCapitalize() {
        assertEquals("Username", StringUtils.capitalize("username"));
        assertEquals("u", StringUtils.capitalize("u"));
        assertEquals("Uu", StringUtils.capitalize("uu"));
        assertEquals(null, StringUtils.capitalize(null));
        assertEquals("", StringUtils.capitalize(""));
    }
}