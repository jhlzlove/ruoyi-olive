package com.olive.base.sysinfo;

import com.olive.base.utils.Arith;
import com.olive.base.utils.LocalDateUtil;

import java.lang.management.ManagementFactory;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Properties;

/**
 * JVM相关信息
 *
 * @author ruoyi
 */
public record Jvm(
        /*  当前JVM占用的内存总数(M) */
        double total,

        /* JVM最大可用内存总数(M) */
        double max,

        /*  JVM空闲内存(M) */
        double free,

        /* 使用内存 */
        double used,

        /* 使用率 */
        double usage,

        /* JDK版本 */
        String version,

        /* JDK路径 */
        String home
) {

    public static Jvm init() {
        Properties props = System.getProperties();
        Runtime runtime = Runtime.getRuntime();
        return new Jvm(
                Arith.div(runtime.totalMemory(), (1024 * 1024), 2),
                Arith.div(runtime.maxMemory(), (1024 * 1024), 2),
                Arith.div(runtime.freeMemory(), (1024 * 1024), 2),
                Arith.div((runtime.totalMemory() - runtime.freeMemory()), (1024 * 1024), 2),
                Arith.mul(Arith.div((runtime.totalMemory() - runtime.freeMemory()), runtime.totalMemory(), 4), 100),
                props.getProperty("java.version"),
                props.getProperty("java.home")
        );
    }

    /**
     * JDK 名称
     */
    public String getName() {
        return ManagementFactory.getRuntimeMXBean().getVmName();
    }

    /**
     * JDK 启动时间
     */
    public String getStartTime() {
        return getServerStartDate().format(LocalDateUtil.DATE_TIME_FORMATTER);
    }

    /**
     * JDK 运行时间
     */
    public String getRunTime() {
        LocalDateTime startTime = getServerStartDate();
        LocalDateTime endTime = LocalDateUtil.dateTime();
        long day = ChronoUnit.DAYS.between(startTime, endTime);
        long hour = ChronoUnit.HOURS.between(startTime, endTime) % 24;
        long min = ChronoUnit.MINUTES.between(startTime, endTime) % 60;
        return day + "天" + hour + "小时" + min + "分钟";
    }

    /**
     * 服务运行参数
     */
    public String getInputArgs() {
        return ManagementFactory.getRuntimeMXBean().getInputArguments().toString();
    }

    /**
     * 获取服务器启动时间
     */
    public LocalDateTime getServerStartDate() {
        long time = ManagementFactory.getRuntimeMXBean().getStartTime();
        return LocalDateTime.ofInstant(Instant.ofEpochMilli(time),  ZoneId.of("Asia/Shanghai"));
    }
}
