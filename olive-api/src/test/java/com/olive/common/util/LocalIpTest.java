package com.olive.common.util;

import org.junit.jupiter.api.Test;

import java.net.*;
import java.util.Enumeration;

/**
 * @author jhlz
 * @version x.x.x
 */
public class LocalIpTest {

    /**
     * example:
     **/
    @Test
    public void iputil_Test() throws UnknownHostException {

        InetAddress loopbackAddress = InetAddress.getLoopbackAddress();
        System.out.println(loopbackAddress.getHostAddress());
        System.out.println(loopbackAddress.getCanonicalHostName());
        System.out.println(loopbackAddress.getHostName());
    }
    /**
     * example:
     **/
    @Test
    public void local_ip_Test() throws SocketException, UnknownHostException {
        // 获取所有工作的网络接口
        Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();
        while (networkInterfaces.hasMoreElements()) {
            NetworkInterface networkInterface = networkInterfaces.nextElement();
            String displayName = networkInterface.getDisplayName();
            // System.out.println("接口显示名称：" + displayName);
            // System.out.println("接口类型：" + networkInterface.getName());
            if (!networkInterface.isLoopback() &&
                    !networkInterface.isVirtual() &&
                    networkInterface.getName().startsWith("wlan")) {
                Enumeration<InetAddress> inetAddresses = networkInterface.getInetAddresses();
                while (inetAddresses.hasMoreElements()) {
                    InetAddress inetAddress = inetAddresses.nextElement();
                    if (!inetAddress.isLoopbackAddress()) {
                        if (inetAddress instanceof Inet4Address net4) {
                            System.out.println("本机 ipv4 " + inetAddress.getHostAddress());
                            System.out.println(inetAddress.getHostName());
                            System.out.println(inetAddress.getCanonicalHostName());
                            System.out.println("=============================");
                        }
                        if (inetAddress instanceof Inet6Address net6) {
                            System.out.println("本机 ipv6 " + inetAddress.getHostAddress());
                            System.out.println(inetAddress.getHostName());
                            System.out.println(inetAddress.getCanonicalHostName());
                            System.out.println("=============================");
                        }
                    }
                }
            }

        }

    }
}
