package com.olive.web.controller.common;

import com.google.code.kaptcha.Producer;
import com.olive.common.utils.uuid.IdUtils;
import com.olive.framework.cache.CacheService;
import com.olive.framework.config.AppConfig;
import com.olive.framework.constant.CacheConstants;
import com.olive.framework.constant.Constants;
import com.olive.framework.exception.ServiceException;
import com.olive.system.service.SysConfigService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import org.springframework.util.FastByteArrayOutputStream;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Base64;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 验证码操作处理
 * 
 * @author ruoyi
 */
@RestController
@AllArgsConstructor
public class CaptchaController {
    @Resource(name = "captchaProducer")
    private Producer captchaProducer;

    @Resource(name = "captchaProducerMath")
    private Producer captchaProducerMath;
    private final SysConfigService configService;
    private final CacheService cacheService;

    /**
     * 生成验证码
     */
    @GetMapping("/captchaImage")
    public Map<String, Object> getCode(HttpServletResponse response) throws IOException {
        boolean captchaEnabled = configService.selectCaptchaEnabled();
        if (!captchaEnabled) {
            return Map.of("captchaEnabled", captchaEnabled);
        }
        // 保存验证码信息
        String uuid = IdUtils.simpleUUID();
        String capStr = null, code = null;
        BufferedImage image = null;
        // 生成验证码
        String captchaType = AppConfig.getCaptchaType();
        if ("math".equals(captchaType)) {
            String capText = captchaProducerMath.createText();
            capStr = capText.substring(0, capText.lastIndexOf("@"));
            code = capText.substring(capText.lastIndexOf("@") + 1);
            image = captchaProducerMath.createImage(capStr);
        } else if ("char".equals(captchaType)) {
            capStr = code = captchaProducer.createText();
            image = captchaProducer.createImage(capStr);
        }

        cacheService.put(CacheConstants.CAPTCHA_CODE_KEY + ":" + uuid, code, Constants.CAPTCHA_EXPIRATION, TimeUnit.MINUTES);
        // 转换流信息写出
        FastByteArrayOutputStream os = new FastByteArrayOutputStream();
        try {
            ImageIO.write(image, "jpg", os);
        } catch (IOException e) {
            throw new ServiceException(e.getMessage());
        }
        return Map.of(
                "captchaEnabled", captchaEnabled,
                "uuid", uuid,
                "img", Base64.getEncoder().encodeToString(os.toByteArray())
        );
    }
}
