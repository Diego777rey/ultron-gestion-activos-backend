package com.dev.ultron.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MacAddressUtilTest {

    @Test
    void normalizaDosPuntosGuionesYSinSeparador() {
        assertEquals("AA:BB:CC:DD:EE:FF", MacAddressUtil.normalize("aa:bb:cc:dd:ee:ff"));
        assertEquals("AA:BB:CC:DD:EE:FF", MacAddressUtil.normalize("aa-bb-cc-dd-ee-ff"));
        assertEquals("AA:BB:CC:DD:EE:FF", MacAddressUtil.normalize("aabbccddeeff"));
    }

    @Test
    void vacioEsNull() {
        assertNull(MacAddressUtil.normalize(null));
        assertNull(MacAddressUtil.normalize("  "));
        assertNull(MacAddressUtil.normalize(""));
    }

    @Test
    void invalidaLanza() {
        assertThrows(IllegalArgumentException.class, () -> MacAddressUtil.normalize("zz:zz:zz:zz:zz:zz"));
        assertThrows(IllegalArgumentException.class, () -> MacAddressUtil.normalize("AA:BB"));
    }

    @Test
    void equalsIgnoraFormato() {
        assertTrue(MacAddressUtil.equalsNormalized("aa-bb-cc-dd-ee-ff", "AA:BB:CC:DD:EE:FF"));
        assertFalse(MacAddressUtil.equalsNormalized("AA:BB:CC:DD:EE:FF", "11:22:33:44:55:66"));
        assertFalse(MacAddressUtil.equalsNormalized(null, "AA:BB:CC:DD:EE:FF"));
    }
}
