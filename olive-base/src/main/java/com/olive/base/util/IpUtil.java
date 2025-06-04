package com.olive.base.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.*;
import java.util.Enumeration;

/**
 * @author jhlz
 * @version x.x.x
 */
public class IpUtil {

    private static final Logger log = LoggerFactory.getLogger(IpUtil.class);

    /**
     * 获取本机的短 hostname（非规范）
     *
     * @return 本机 hostname
     */
    public static String getHostName() {
        try {
            return InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 获取本机 IP
     *
     * @return 本机 IP
     */
    public static String getLocalIp() {
        Enumeration<NetworkInterface> networkInterfaces = getNetworkInterfaces();
        while (networkInterfaces.hasMoreElements()) {
            NetworkInterface network = networkInterfaces.nextElement();
            try {
                // 过滤掉虚拟网络（VPN）和环回地址
                if (!network.isVirtual() && !network.isLoopback()) {
                    Enumeration<InetAddress> inetAddresses = network.getInetAddresses();
                    if (inetAddresses.hasMoreElements()) {
                        InetAddress address = inetAddresses.nextElement();
                        log.info("{}", address.getHostAddress());
                        return address.getHostAddress();
                    }
                }
            } catch (SocketException e) {
                throw new RuntimeException(e);
            }
        }
        return null;
    }

    /**
     * 获取本地环回 IP
     *
     * @return IP v4
     */
    public static String getLoopbackIp() {
        Enumeration<NetworkInterface> networkInterfaces = getNetworkInterfaces();
        while (networkInterfaces.hasMoreElements()) {
            NetworkInterface network = networkInterfaces.nextElement();
            try {
                if (network.isLoopback()) {
                    Enumeration<InetAddress> inetAddresses = network.getInetAddresses();
                    if (inetAddresses.hasMoreElements()) {
                        InetAddress address = inetAddresses.nextElement();
                        return address.getHostAddress();
                    }
                }
            } catch (SocketException e) {
                throw new RuntimeException(e);
            }
        }
        return null;
    }

    /**
     * 获取本机所有工作的网络接口
     *
     * @return 工作的网络接口枚举
     */
    public static Enumeration<NetworkInterface> getNetworkInterfaces() {
        try {
            return NetworkInterface.getNetworkInterfaces();
        } catch (SocketException e) {
            throw new RuntimeException(e);
        }
    }

}
