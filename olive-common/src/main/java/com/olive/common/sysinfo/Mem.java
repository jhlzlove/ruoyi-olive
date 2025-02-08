package com.olive.common.sysinfo;

import com.olive.common.utils.Arith;
import oshi.SystemInfo;
import oshi.hardware.GlobalMemory;

/**
 * 內存相关信息
 *
 * @author ruoyi
 */
public record Mem(
        /*  内存总量 */
        double total,

        /* 已用内存 */
        double used,

        /* 使用率 */
        double usage,

        /*  剩余内存 */
        double free
) {

    public static Mem init() {
        SystemInfo si = new SystemInfo();
        GlobalMemory memory = si.getHardware().getMemory();
        double total = Arith.div(memory.getTotal(), (1024 * 1024 * 1024), 2);
        double used = Arith.div((memory.getTotal() - memory.getAvailable()), (1024 * 1024 * 1024), 2);
        double free = Arith.div( memory.getAvailable(), (1024 * 1024 * 1024), 2);
        double usage = Arith.mul(Arith.div(used, total, 4), 100);
        return new Mem(total, used, usage, free);
    }
}
