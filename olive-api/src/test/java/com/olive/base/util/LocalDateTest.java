package com.olive.base.util;

import com.olive.base.utils.LocalDateUtil;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.lang.management.ManagementFactory;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.time.temporal.ChronoUnit;

/**
 * @author jhlz
 * @version x.x.x
 */
public class LocalDateTest {

    /**
     * example:
     **/
    @Test
    public void local_date_util_toStr_Test() {
        System.out.println(LocalDateUtil.toStr(LocalDate.now()));
        System.out.println(LocalDateUtil.toStr(LocalDateTime.now()));
        System.out.println(LocalDateTime.now());
        System.out.println(LocalDate.now());
    }

    /**
     * example:
     **/
    @Test
    public void parse_long_time_Test() {
        long startTime = ManagementFactory.getRuntimeMXBean().getStartTime();
        Instant instant = Instant.ofEpochMilli(startTime);
        System.out.println(LocalDateTime.ofInstant(instant, ZoneId.of("Asia/Shanghai")));
    }

    /**
     * example:
     **/
    @Test
    public void two_localdatetime_cha_Test() {
        LocalDateTime startTime = LocalDateTime.of(2022, 1, 1, 12, 0, 0);
        LocalDateTime endTime = LocalDateTime.of(2022, 1, 3, 14, 30, 0);
        long day = ChronoUnit.DAYS.between(startTime, endTime);
        long hour = ChronoUnit.HOURS.between(startTime, endTime) % 24;
        long minute = ChronoUnit.MINUTES.between(startTime, endTime) % 60;
        Assertions.assertEquals(2, day);
        Assertions.assertEquals(2, hour);
        Assertions.assertEquals(30, minute);
    }

    /**
     * example:
     **/
    @Test
    public void date_format_Test() {
        String format = LocalDateUtil.date().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
        System.out.println(format);
    }
    /**
     * example:
     **/
    @Test
    public void index_Test() {
        System.out.println(LocalDateUtil.dateTime());
    }

    /**
     * example:
     **/
    @Test
    public void LocalDate_Test() {
        String format = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM).format(LocalDateTime.now());
        String full = DateTimeFormatter.ofLocalizedDate(FormatStyle.FULL).format(LocalDate.now());
        String shortTime = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT).format(LocalDateTime.now());
        String longTime = DateTimeFormatter.ofLocalizedDate(FormatStyle.LONG).format(LocalDate.now());
        System.out.println(full);
        System.out.println(shortTime);
        System.out.println(longTime);
        System.out.println(format);
    }
}
