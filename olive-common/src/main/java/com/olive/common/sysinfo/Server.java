package com.olive.common.sysinfo;

import java.util.List;

/**
 * 服务器相关信息
 *
 * @author ruoyi
 */
public record Server(
        Cpu cpu,
        Mem mem,
        Jvm jvm,
        Sys sys,
        List<SysFile> sysFiles
) {

    public static Server create() {
        return new Server(Cpu.init(), Mem.init(), Jvm.init(), Sys.init(), SysFile.init());
    }
}
