package com.olive.base.util;

import com.olive.model.LoginUser;
import com.olive.model.SysUser;
import com.olive.service.util.JSON;
import org.junit.jupiter.api.Test;

/**
 * @author jhlz
 * @version 0.0.1
 */
public class JimmerEntityTest {
    /**
     * example:
     */
    @Test
    public void jimmer_entity_test() {
        String json = """
                {
                  "userName": "admin",
                  "password": "admin123"
                }
                """;
        SysUser sysUser = null;
        sysUser = JSON.toObj(json, SysUser.class);
        var user = new LoginUser(1L, 100L, sysUser, null);
        System.out.println(user);

        // try {
        //     // sysUser = ImmutableObjects.fromString(SysUser.class, json);
        //
        // } catch (JsonProcessingException e) {
        //     throw new RuntimeException(e);
        // }
        System.out.println(sysUser);
        // SysUserDraft.$.produce(draft -> {
        //     draft.
        // })
    }
}
