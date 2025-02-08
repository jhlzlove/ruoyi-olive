package com.olive;

import com.olive.common.utils.IpUtil;
import org.babyfish.jimmer.client.EnableImplicitApi;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.core.env.Environment;

/**
 * 启动程序
 *
 * @author ruoyi
 */
@EnableImplicitApi
// 表示通过aop框架暴露该代理对象,AopContext能够访问
@EnableAspectJAutoProxy(exposeProxy = true)
@SpringBootApplication
public class OliveApplication {
    public static void main(String[] args) {
        ApplicationContext application = SpringApplication.run(OliveApplication.class, args);
        Environment env = application.getEnvironment();
        String ip = IpUtil.getLocalIp();
        String port = env.getProperty("server.port");
        String docPath = env.getProperty("jimmer.client.openapi.ui-path");
        String ymlPath = env.getProperty("jimmer.client.openapi.path");
        String text = """
                ----------------------------------------------------------
                Application running success!
                Local:          http://localhost:%s
                External:       http://%s:%s
                YAML可导入文件:  http://%s:%s%s
                Swagger文档:    http://%s:%s%s
                ----------------------------------------------------------
                """.formatted(port, ip, port, ip, port, ymlPath, ip, port, docPath);
        System.out.println(text);
    }
}
