package com.olive.web.controller.monitor;

import com.olive.common.sysinfo.Server;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 服务器监控
 *
 * @author ruoyi
 */
@RestController
@RequestMapping("/monitor/server")
public class ServerController {

    private static final Logger log = LoggerFactory.getLogger(ServerController.class);

    /**
     * 监控详情
     * @return AjaxResult
     * @throws Exception 异常
     */
    // @PreAuthorize("@ss.hasPermi('monitor:server:list')")
    @GetMapping
    public Server getInfo() {
        return Server.create();
    }
}
