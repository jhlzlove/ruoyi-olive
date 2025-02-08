package com.olive.framework.web.system;

import org.babyfish.jimmer.sql.*;
import org.jetbrains.annotations.Nullable;

import java.time.LocalDateTime;

/**
 * 操作日志记录表 oper_log
 *
 * @author ruoyi
 */
@Entity
@Table(name = "sys_oper_log")
public interface SysOperLog {

    /**
     * 日志主键
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    long operId();

    /**
     * 操作模块
     */
    @Nullable
    String title();

    /**
     * 业务类型（0其它 1新增 2修改 3删除）
     */
    @Nullable
    Integer businessType();

    /**
     * 请求方法
     */
    @Nullable
    String method();

    /**
     * 请求方式
     */
    @Nullable
    String requestMethod();

    /**
     * 操作类别（0其它 1后台用户 2手机端用户）
     */
    @Nullable
    Integer operatorType();

    /**
     * 操作人员
     */
    String operName();

    /**
     * 部门名称
     */
    @Nullable
    String deptName();

    /**
     * 请求url
     */
    @Nullable
    String operUrl();

    /**
     * 操作地址
     */
    @Nullable
    String operIp();

    /**
     * 操作地点
     */
    @Nullable
    String operLocation();

    /**
     * 请求参数
     */
    @Nullable
    String operParam();

    /**
     * 返回参数
     */
    @Nullable
    String jsonResult();

    /**
     * 操作状态（0正常 1异常）
     */
    @Nullable
    Integer status();

    /**
     * 错误消息
     */
    @Nullable
    String errorMsg();

    /**
     * 操作时间
     */
    @Nullable
    LocalDateTime operTime();

    /**
     * 消耗时间
     */
    @Nullable
    Long costTime();

}
