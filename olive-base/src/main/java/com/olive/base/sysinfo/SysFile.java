package com.olive.base.sysinfo;

import com.olive.base.utils.Arith;
import oshi.SystemInfo;
import oshi.software.os.FileSystem;
import oshi.software.os.OSFileStore;
import oshi.software.os.OperatingSystem;

import java.util.List;

/**
 * 系统文件相关信息
 *
 * @author ruoyi
 */
public record SysFile(
        /* 盘符路径 */
        String dirName,

        /* 盘符类型 */
        String sysTypeName,

        /* 文件类型 */
        String typeName,

        /* 总大小 */
        String total,

        /* 剩余大小 */
        String free,

        /* 已经使用量 */
        String used,

        /* 资源的使用率 */
        double usage
) {

    public static List<SysFile> init() {
        OperatingSystem os = new SystemInfo().getOperatingSystem();
        FileSystem fileSystem = os.getFileSystem();
        List<OSFileStore> fsArray = fileSystem.getFileStores();
        return fsArray.stream().map(fs -> {
            long free = fs.getUsableSpace();
            long total = fs.getTotalSpace();
            long used = total - free;
            return new SysFile(
                    fs.getMount(),
                    fs.getType(),
                    fs.getName(),
                    convertFileSize(total),
                    convertFileSize(free),
                    convertFileSize(used),
                    Arith.mul(Arith.div(used, total, 4), 100));
        }).toList();
    }

    /**
     * 字节转换
     *
     * @param size 字节大小
     * @return 转换后值
     */
    public static String convertFileSize(long size) {
        long kb = 1024;
        long mb = kb * 1024;
        long gb = mb * 1024;
        if (size >= gb) {
            return String.format("%.1f GB", (float) size / gb);
        } else if (size >= mb) {
            float f = (float) size / mb;
            return String.format(f > 100 ? "%.0f MB" : "%.1f MB", f);
        } else if (size >= kb) {
            float f = (float) size / kb;
            return String.format(f > 100 ? "%.0f KB" : "%.1f KB", f);
        } else {
            return String.format("%d B", size);
        }
    }
}
