package com.olive.common.utils;

import org.apache.commons.lang3.StringUtils;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.Temporal;

/**
 * jsr310 日期格式工具类
 *
 * @author jhlz
 * @version 1.0.0
 */
public class LocalDateUtil {

    private static final Clock clock = Clock.system(ZoneId.of("Asia/Shanghai"));
    // 常用格式
    public static final String DATE_TIME_PATTERN = "yyyy-MM-dd HH:mm:ss";
    public static final String DATE_PATTERN = "yyyy-MM-dd";
    public static final String TIME_PATTERN = "HH:mm:ss";
    // 其它格式
    public static final String YYYY_MM_DD = "yyyy-MM-dd";
    public static final String YYYYMMDDHHMMSS = "yyyyMMddHHmmss";

    public static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern(DATE_TIME_PATTERN);
    public static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern(DATE_PATTERN);

    /**
     * 获取当前的 LocalDateTime
     * @return LocalDateTime
     */
    public static LocalDateTime dateTime() {
        return LocalDateTime.now(clock);
    }

    /**
     * 获取当前的 LocalDate
     * @return LocalDate
     */
    public static LocalDate date() {
        return LocalDate.now(clock);
    }

    /**
     *
     * @param temporal
     * @return
     */
    public static String toStr(Temporal temporal) {
        if (temporal instanceof LocalDate) {
            return DateTimeFormatter.ofPattern(DATE_PATTERN).format(temporal);
        }
        if (temporal instanceof LocalDateTime) {
            return DateTimeFormatter.ofPattern(DATE_TIME_PATTERN).format(temporal);
        }
        return null;
    }

    /**
     * LocalDate 日期字符串解析为 LocalDateTime 格式
     *
     * @param str  LocalDate 格式字符串
     * @return  LocalDateTime，末尾为 00:00:00
     */
    public static LocalDateTime dateStrToDateTime(String str) {
        return strToDate(str).atStartOfDay();
    }

    /**
     * 使用默认格式解析字符串为 LocalDate 格式
     *
     * @param str   日期字符串
     * @return LocalDate
     */
    public static LocalDate strToDate(String str) {
        return strToDate(str, DATE_PATTERN);
    }

    /**
     * 根据指定的 pattern 解析日期字符串为 LocalDate
     *
     * @param str 日期字符串
     * @param pattern 字符串日期格式，默认为 yyyy-MM-dd
     * @return LocalDate 日期
     */
    public static LocalDate strToDate(String str, String pattern) {
        String format = StringUtils.isNotEmpty(pattern) ? pattern : DATE_PATTERN;
        return LocalDate.parse(str, DateTimeFormatter.ofPattern(format));
    }

    /**
     * LocalDate 转为指定格式的字符串日期
     *
     * @param date    LocalDate 日期
     * @param pattern 格式，默认为 yyyy-MM-dd
     * @return 指定格式的字符串日期
     */
    public static String dateToStr(LocalDate date, String pattern) {
        DateTimeFormatter.ofPattern(pattern).format(date);
        String format = StringUtils.isNotEmpty(pattern) ? pattern : DATE_PATTERN;
        return date.format(DateTimeFormatter.ofPattern(format));
    }

    /**
     * 根据指定的 pattern 解析日期字符串为 LocalDateTime
     *
     * @param date    字符串日期
     * @param pattern 字符串日期格式
     * @return LocalDateTime
     */
    public static LocalDateTime strToDateTime(String date, String pattern) {
        String format = StringUtils.isNotEmpty(pattern) ? pattern : DATE_TIME_PATTERN;
        return LocalDateTime.parse(date, DateTimeFormatter.ofPattern(format));
    }

    /**
     * LocalDateTime 格式化为指定格式的字符串日期
     *
     * @param date    LocalDateTime 日期
     * @param pattern 格式，默认为 yyyy-MM-dd HH:mm:ss
     * @return 指定格式的字符串日期
     */
    public static String dateTimeToStr(LocalDateTime date, String pattern) {
        String format = StringUtils.isNotEmpty(pattern) ? pattern : DATE_TIME_PATTERN;
        return date.format(DateTimeFormatter.ofPattern(format));
    }
}