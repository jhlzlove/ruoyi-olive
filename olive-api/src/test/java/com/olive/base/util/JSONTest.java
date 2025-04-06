package com.olive.base.util;

import lombok.Getter;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

public class JSONTest {

    /**
     * example:
     **/
    @Test
    public void enum_Test() {
        System.out.println(Gender.MALE.getDesc());
        System.out.println(Gender.MALE);
        Assertions.assertEquals("MALE", Gender.MALE.name());
    }
    @Getter
    enum Gender {
        MALE("男"),
        FEMALE("女");
        private final String desc;

        Gender(String desc) {
            this.desc = desc;
        }
    }

    /**
     * example:
     **/
    @Test
    public void list_stream_Test() {
        Stream<Integer> stream = Stream.of(1, 2, 3, 4, 5);
        List<Integer> list = stream.filter(it -> it > 3).toList();
        System.out.println(list);
    }

    /**
     * example:
     **/
    @Test
    public void stream_Test() {
        List<Map<String, Object>> list = new ArrayList<>();
        HashMap<String, Object> map = new HashMap<>();
        map.put("add", 1);
        map.put("insert", 2);
        map.put("update", 3);
        map.put("delete", 4);
        map.put("save", 5);
        list.add(map);
        // String o = list.stream()
        //         .filter(it -> Objects.nonNull(it.get("add")))
        //         .map(it -> it.get("add"))
        //         .filter(Objects::nonNull)
        //         .findFirst()
        //         .orElseGet(String::new).toString();
        // Assertions.assertEquals("", o);
    }
    /**
     * example:
     **/
    @Test
    public void type_instance_Test() {
        String str = "诗酒趁年华";
        try {
            int value = Integer.parseInt(str);
            System.out.println(value);
            // 处理成功转换的值
        } catch (NumberFormatException e) {
            // 处理转换失败的情况
            System.out.println(str);
        }
    }

    @Test
    public void test() {
        System.out.println("Current user: " + System.getProperty("user.name"));
        System.out.println("Current user: " + System.getProperty("os.name"));
        String server = "mysql";
        String command = "Start-Process powershell -Verb runAs -ArgumentList '-ExecutionPolicy Bypass -Command \"net stop %s && net start %s\"'";
        String[] comd = {
                "powershell",
                "-ExecutionPolicy", "Bypass",
                "-Command", String.format(command, server, server),
        };
        ProcessBuilder processBuilder = new ProcessBuilder(comd);
        // 错误重定向到标准输出
        processBuilder.redirectErrorStream(true);
        // 字符集，win 果然与众不同！
        Charset gbk = Charset.forName("GBK");
        Process exec = null;
        try {
            // 执行命令
            exec = processBuilder.start();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        try (
                BufferedReader reader = new BufferedReader(new InputStreamReader(exec.getInputStream(), gbk));
                BufferedReader errorReader = new BufferedReader(new InputStreamReader(exec.getErrorStream(), gbk));
        ) {
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
            }

            String errorLine;
            while ((errorLine = errorReader.readLine()) != null) {
                System.out.println(errorLine);
            }
            int exitCode = exec.waitFor();
            System.out.println("Process exited with code: " + exitCode);
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    record Person(String name, int age) {
    }

    public static void main(String[] args) {
        String command = "sudo podman restart mysql8";
        String[] commandArr = {"/bin/sh", "-c", command};
        ProcessBuilder processBuilder = new ProcessBuilder(commandArr);
        processBuilder.redirectErrorStream(true);
        Process process = null;
        try {
            process = processBuilder.start();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        try (BufferedReader isr = new BufferedReader(new InputStreamReader(process.getInputStream()));
             BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
        ) {
            String line;
            while ((line = isr.readLine()) != null) {
                System.out.println(line);
            }
            String errorLine;
            while ((errorLine = errorReader.readLine()) != null) {
                System.out.println(errorLine);
            }

            int i = process.waitFor();
            System.out.println("Process exited with code: " + i);
        } catch (InterruptedException | IOException e) {
            throw new RuntimeException(e);
        }
    }
}
