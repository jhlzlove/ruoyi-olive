package com.olive.base.util;

import org.junit.jupiter.api.Test;

import java.util.List;

/**
 * @author jhlz
 * @version 0.0.1
 */
public class JDKTest {
    /**
     * example: list filter
     */
    @Test
    public void list_filter_test() {
        List<Integer> list = List.of(1, 2, 3, 4);
        list.stream().filter(it -> it > 3)
                .forEach(System.out::println);

    }
}
