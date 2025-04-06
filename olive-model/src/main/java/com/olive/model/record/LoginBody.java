package com.olive.model.record;


/**
 * 用户登录对象
 * 
 * @author ruoyi
 */
public record LoginBody (
         // 用户名
         String username,
         // 密码
         String password,
         // 唯一标识
         String uuid,
         // 手机号
         String phonenumber,
         // 邮箱
         String email,
         // 验证码
         String code
) {}
