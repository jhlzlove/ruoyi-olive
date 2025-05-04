package com.olive.service.util.ip;

import com.olive.service.util.JSON;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;

/**
 * 获取地址类
 *
 * @author ruoyi
 */
public class AddressUtils {
    private static final Logger log = LoggerFactory.getLogger(AddressUtils.class);

    // IP地址查询
    public static final String IP_URL = "http://whois.pconline.com.cn/ipJson.jsp";

    // 未知地址
    public static final String UNKNOWN = "XX XX";

    public static String getRealAddressByIP(String ip) {
        if (IpUtils.internalIp(ip)) {
            return "内网IP";
        }
        String uri = IP_URL + "?ip=" + ip + "&json=true";
        HttpRequest request = HttpRequest.newBuilder(URI.create(uri)).GET().build();

        try (HttpClient httpClient = HttpClient.newHttpClient()){
            String resp = httpClient
                    .send(request, HttpResponse.BodyHandlers.ofString())
                    .body();
            if (StringUtils.isEmpty(resp)) {
                return UNKNOWN;
            }
            Map<String, String> obj = JSON.toMap(resp, String.class);
            String region = obj.get("pro");
            String city = obj.get("city");
            return String.format("%s %s", region, city);
        } catch (Exception e) {
            log.error("获取地理位置异常 {}", ip);
        }
        return UNKNOWN;
    }
}
