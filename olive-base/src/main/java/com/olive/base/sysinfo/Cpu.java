package com.olive.base.sysinfo;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.olive.base.utils.Arith;
import oshi.SystemInfo;
import oshi.hardware.CentralProcessor;
import oshi.hardware.HardwareAbstractionLayer;
import oshi.util.Util;

/**
 * CPU相关信息
 *
 * @author ruoyi
 */
public record Cpu(
        /* 核心数 */
        int cpuNum,

        /* CPU总的使用率 */
        double total,

        /* CPU系统使用率 */
        double sys,

        /*  CPU用户使用率 */
        double used,

        /* CPU当前等待率 */
        @JsonProperty("wait")
        double waitRate,

        /*  CPU当前空闲率 */
        double free
) {

    public static Cpu init() {
        SystemInfo si = new SystemInfo();
        HardwareAbstractionLayer hal = si.getHardware();
        // CPU信息
        CentralProcessor processor = hal.getProcessor();
        long[] prevTicks = processor.getSystemCpuLoadTicks();
        Util.sleep(1000);
        long[] ticks = processor.getSystemCpuLoadTicks();
        double nice = ticks[CentralProcessor.TickType.NICE.getIndex()] - prevTicks[CentralProcessor.TickType.NICE.getIndex()];
        double irq = ticks[CentralProcessor.TickType.IRQ.getIndex()] - prevTicks[CentralProcessor.TickType.IRQ.getIndex()];
        double softirq = ticks[CentralProcessor.TickType.SOFTIRQ.getIndex()] - prevTicks[CentralProcessor.TickType.SOFTIRQ.getIndex()];
        double steal = ticks[CentralProcessor.TickType.STEAL.getIndex()] - prevTicks[CentralProcessor.TickType.STEAL.getIndex()];
        double cSys = ticks[CentralProcessor.TickType.SYSTEM.getIndex()] - prevTicks[CentralProcessor.TickType.SYSTEM.getIndex()];
        double used = ticks[CentralProcessor.TickType.USER.getIndex()] - prevTicks[CentralProcessor.TickType.USER.getIndex()];
        double iowait = ticks[CentralProcessor.TickType.IOWAIT.getIndex()] - prevTicks[CentralProcessor.TickType.IOWAIT.getIndex()];
        double idle = ticks[CentralProcessor.TickType.IDLE.getIndex()] - prevTicks[CentralProcessor.TickType.IDLE.getIndex()];
        double totalCpu = used + nice + cSys + idle + iowait + irq + softirq + steal;
        return new Cpu(processor.getLogicalProcessorCount(),
                Arith.round(Arith.mul(totalCpu, 100), 2),
                Arith.round(Arith.mul((cSys / totalCpu), 100), 2),
                Arith.round(Arith.mul((used / totalCpu), 100), 2),
                Arith.round(Arith.mul((iowait / totalCpu), 100), 2),
                Arith.round(Arith.mul((idle / totalCpu), 100), 2)
        );
    }

}
