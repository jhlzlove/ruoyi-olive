package com.olive.base.util;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.text.MessageFormat;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author jhlz
 * @version x.x.x
 */
public class StringTest {

    /**
     * example: remove
     */
    @Test
    public void remove_start_test() {
        String str = "sys_config:sys.index.sideTheme";
        String name = "sys_config";
        String s = StringUtils.removeStart(str, name + ":");
        Assertions.assertEquals("sys.index.sideTheme", s);
    }

    @Test
    void message_format_test() {
        String format1 = MessageFormat.format("{0}/{1}_{2}.{3}",
                DateFormatUtils.format(new Date(), "yyyy/MM/dd"), "hello",
                "world",
                "test"
        );
        assertEquals("2025/03/22/hello_world.test", format1);
    }

    @Test
    public void string() {
        String str = "Hello World! hello";
        assertEquals("H World! h",
                StringUtils.remove(str, "ello")
        );
    }
}
