package com.olive.base.util;

import org.junit.jupiter.api.Test;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

/**
 * @author jhlz
 * @version x.x.x
 */
public class IpTest {
    /**
     * example: get local ip
     */
    @Test
    public void get_local_ip_test() {
        Enumeration<NetworkInterface> networkInterfaces = getNetworkInterfaces();
        while (networkInterfaces.hasMoreElements()) {
            NetworkInterface network = networkInterfaces.nextElement();
            try {
                // 过滤掉虚拟网络（VPN）和环回地址
                if (!network.isVirtual() && !network.isLoopback()) {
                    Enumeration<InetAddress> inetAddresses = network.getInetAddresses();
                    if (inetAddresses.hasMoreElements()) {
                        InetAddress address = inetAddresses.nextElement();
                        System.out.println(address.getHostAddress());
                    }
                }
            } catch (SocketException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public static Enumeration<NetworkInterface> getNetworkInterfaces() {
        try {
            return NetworkInterface.getNetworkInterfaces();
        } catch (SocketException e) {
            throw new RuntimeException(e);
        }
    }
}
