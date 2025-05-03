package com.olive.api.system;

import com.olive.service.aop.log.Log;
import com.olive.model.LoginUser;
import com.olive.model.SysUser;
import com.olive.model.SysUserDraft;
import com.olive.model.constant.BusinessType;
import com.olive.model.exception.SecurityException;
import com.olive.model.exception.SysUserException;
import com.olive.service.SysUserService;
import com.olive.service.TokenService;
import com.olive.service.security.SecurityUtils;
import lombok.AllArgsConstructor;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.Map;

/**
 * 个人信息 业务处理
 *
 * @author ruoyi
 */
@RestController
@AllArgsConstructor
@RequestMapping("/system/user/profile")
public class SysProfileController {
    private final SysUserService userService;
    private final TokenService tokenService;

    /**
     * 个人信息
     */
    @GetMapping
    public Map<String, Object> profile() {
        LoginUser loginUser = SecurityUtils.getLoginUser();
        return Map.of(
                "data", loginUser.getUser(),
                "roleGroup", userService.selectUserRoleGroup(loginUser.getUsername()),
                "postGroup", userService.selectUserPostGroup(loginUser.getUsername())
        );
    }

    /**
     * 修改用户
     */
    @Log(title = "个人信息", businessType = BusinessType.UPDATE)
    @PutMapping
    public boolean updateProfile(@RequestBody SysUser user) {
        LoginUser loginUser = SecurityUtils.getLoginUser();
        SysUser sysUser = loginUser.getUser();
        if (StringUtils.isNotEmpty(user.phonenumber()) && !userService.checkPhoneUnique(user)) {
            throw SysUserException.phoneExist("修改用户失败，手机号码已存在", user.phonenumber());
        }
        if (StringUtils.isNotEmpty(user.email()) && !userService.checkEmailUnique(user)) {
            throw SysUserException.emailExist("修改用户失败，邮箱账号已存在", user.email());
        }
        SysUser temp = SysUserDraft.$.produce(draft ->
                draft.setUserName(sysUser.userName())
                        .setUserId(sysUser.userId())
        );
        if (userService.updateUserProfile(temp) > 0) {
            // 更新缓存用户信息
            // sysUser.setNickName(user.nickName());
            // sysUser.setPhonenumber(user.phonenumber());
            // sysUser.setEmail(user.email());
            // sysUser.setSex(user.sex());
            tokenService.setLoginUser(loginUser);
            return true;
        }
        return false;
    }

    /**
     * 重置密码
     */
    @Log(title = "个人信息", businessType = BusinessType.UPDATE)
    @PutMapping("/updatePwd")
    public boolean updatePwd(@RequestParam String oldPassword, @RequestParam String newPassword) {
        LoginUser loginUser = SecurityUtils.getLoginUser();
        String userName = loginUser.getUsername();
        String password = loginUser.getPassword();
        if (!SecurityUtils.matchesPassword(oldPassword, password)) {
            throw SecurityException.oldPasswordError("修改密码失败，旧密码错误");
        }
        if (SecurityUtils.matchesPassword(newPassword, password)) {
            throw SecurityException.sameOldPassword("新密码不能与旧密码相同");
        }
        newPassword = SecurityUtils.encryptPassword(newPassword);
        if (userService.resetUserPwd(userName, newPassword) > 0) {
            // 更新缓存用户密码
            // SysUser temp = loginUser.getSysUser();
            // temp.setPassword(newPassword);
            tokenService.setLoginUser(loginUser);
            return true;
        }
        return false;
    }

    /**
     * 头像上传
     */
    @Log(title = "用户头像", businessType = BusinessType.UPDATE)
    @PostMapping("/avatar")
    public Map<String, Object> avatar(@RequestParam("avatarfile") MultipartFile file) throws Exception {
        if (!file.isEmpty()) {
            LoginUser loginUser = SecurityUtils.getLoginUser();
            String extractPath = loginUser.getUsername() + File.separator + loginUser.getUserId();
            String fileName = "avatar." + FilenameUtils.getExtension(file.getOriginalFilename());
            String avatar = "";
            // String avatar = FileOperateUtils.upload(AppConfig.getAvatarPath() + extractPath, fileName, file,
            //         MimeTypeUtils.IMAGE_EXTENSION);
            if (userService.updateUserAvatar(loginUser.getUsername(), avatar) > 0) {
                tokenService.setLoginUser(loginUser);
                return Map.of("imgUrl", avatar);
            }
        }
        throw new RuntimeException("上传图片异常，请联系管理员");
    }
}
