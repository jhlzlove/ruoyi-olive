package com.olive.common.util;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * @author jhlz
 * @version x.x.x
 */
public class StringTest {
    /**
     * example:
     **/
    @Test
    public void string_format_Test() {
        String format = String.format("%d_%d_%d-%d", 1, 2,3, 4);
        Assertions.assertEquals("1_2_3-4", format);
    }
}
