package com.olive.base.util;

import com.olive.model.SysLoginLog;
import com.olive.model.SysLoginLogDraft;
import org.babyfish.jimmer.meta.ImmutableProp;
import org.babyfish.jimmer.meta.ImmutableType;
import org.babyfish.jimmer.runtime.ImmutableSpi;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Collection;

/**
 * @author jhlz
 * @version x.x.x
 */
public class JimmerTest {

    /**
     * example:
     **/
    @Test
    public void immutable_type_Test() {
        SysLoginLog log = new SysLoginLogDraft.Builder()
                .infoId(1L)
                .userName("admin")
                .browser("Firefox")
                .os("Linux")
                .ipaddr("127.0.0.1")
                .loginTime(LocalDateTime.now())
                .build();
        Collection<ImmutableProp> values = ImmutableType.get(log.getClass()).getProps().values();
        System.out.println(values);
        ImmutableSpi e = (ImmutableSpi) log;

        values.forEach(it -> {
            if (e.__isLoaded(it.getId())) {
                System.out.println(it.getId() + " : " + it.getName() + " - " + e.__get(it.getId()));
            }
        });

    }
}
