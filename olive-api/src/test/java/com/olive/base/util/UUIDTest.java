package com.olive.base.util;

import com.olive.base.util.uuid.UUID;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * @author jhlz
 * @version 0.0.1
 */
public class UUIDTest {

    /**
     * example: uuid test
     */
    @Test
    public void uuid_test() {
        Assertions.assertEquals(32, UUID.fastUUID().toString().replaceAll("-", "").length());
    }
}
