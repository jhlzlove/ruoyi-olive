package com.olive.model;

import com.olive.model.xss.Xss;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.babyfish.jimmer.sql.*;

/**
 * 通知公告表 sys_notice
 *
 * @author ruoyi
 */
@Entity
@Table(name = "sys_notice")
public interface SysNotice extends BaseEntity {

    /**
     * 公告ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    long noticeId();

    /**
     * 公告标题
     */
    @Xss(message = "公告标题不能包含脚本字符")
    @NotBlank(message = "公告标题不能为空")
    @Size(min = 0, max = 50, message = "公告标题不能超过50个字符")
    String noticeTitle();

    /**
     * 公告类型（1通知 2公告）
     */
    String noticeType();

    /**
     * 公告内容
     */
    String noticeContent();

    /**
     * 公告状态（0正常 1关闭）
     */
    String status();

}
