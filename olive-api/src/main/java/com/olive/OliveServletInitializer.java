package com.olive;

import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;

/**
 * 打 war 包使用，现在使用 boot 的项目一般是 jar 包了
 * 使用不上但保留
 *
 * @author ruoyi
 */
public class OliveServletInitializer extends SpringBootServletInitializer {
    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
        return application.sources(OliveApplication.class);
    }
}
