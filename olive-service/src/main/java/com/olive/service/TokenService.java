package com.olive.service;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTCreator;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.Claim;
import com.olive.base.util.uuid.IdUtils;
import com.olive.model.LoginUser;
import com.olive.model.constant.CacheConstant;
import com.olive.model.constant.AppConstant;
import com.olive.service.config.AppProperties;
import com.olive.service.util.JSON;
import com.olive.service.util.ip.AddressUtils;
import com.olive.service.util.ip.IpUtils;
import eu.bitwalker.useragentutils.UserAgent;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * token验证处理
 *
 * @author ruoyi
 */
@Component
public class TokenService {
    private static final Logger log = LoggerFactory.getLogger(TokenService.class);
    // 令牌自定义标识
    // @Value("${token.header}")
    // private String header;
    //
    // // 令牌秘钥
    // @Value("${token.secret}")
    // private String secret;
    //
    // // 令牌有效期（默认30分钟）
    // @Value("${token.expireTime}")
    // private int expireTime;

    protected static final long MILLIS_SECOND = 1000;

    protected static final long MILLIS_MINUTE = 60 * MILLIS_SECOND;

    private static final Long MILLIS_MINUTE_TEN = 20 * 60 * 1000L;

    private final CacheManager cacheManager;
    private final AppProperties appProperties;

    public TokenService(CacheManager cacheManager, AppProperties appProperties) {
        this.cacheManager = cacheManager;
        this.appProperties = appProperties;
    }

    /**
     * 获取用户身份信息
     *
     * @return 用户信息
     */
    public LoginUser getLoginUser(HttpServletRequest request) {
        // 获取请求携带的令牌
        String token = getToken(request);
        if (StringUtils.isNotEmpty(token)) {
            try {
                Map<String, Claim> claims = parseToken(token);
                // 解析对应的权限以及用户信息
                String claimToken = claims.get(AppConstant.LOGIN_USER_KEY).asString();
                Cache cache = cacheManager.getCache(CacheConstant.CACHE_LOGIN_TOKEN_KEY);
                String cacheUser = cache.get(claimToken, String.class);
                if (StringUtils.isNotEmpty(cacheUser)) {
                    return JSON.toObj(cacheUser, LoginUser.class);
                }
            } catch (Exception e) {


            }
        }
        return null;
    }

    /**
     * 设置用户身份信息
     */
    public void setLoginUser(LoginUser loginUser) {
        if (Objects.nonNull(loginUser) && StringUtils.isNotEmpty(loginUser.getToken())) {
            refreshToken(loginUser);
        }
    }

    /**
     * 删除用户身份信息
     */
    public void delLoginUser(String token) {
        if (StringUtils.isNotEmpty(token)) {
            cacheManager.getCache(CacheConstant.CACHE_LOGIN_TOKEN_KEY).evict(token);
        }
    }

    /**
     * 创建令牌
     *
     * @param loginUser 用户信息
     * @return 令牌
     */
    public String createToken(LoginUser loginUser) {
        String token = IdUtils.fastUUID();
        loginUser.setToken(token);
        setUserAgent(loginUser);
        refreshToken(loginUser);
        Map<String, String> claims = new HashMap<>();
        claims.put(AppConstant.LOGIN_USER_KEY, token);
        return createToken(claims);
    }

    /**
     * 验证令牌有效期，相差不足20分钟，自动刷新缓存
     *
     * @param loginUser
     * @return 令牌
     */
    public void verifyToken(LoginUser loginUser) {
        long expireTime = loginUser.getExpireTime();
        long currentTime = System.currentTimeMillis();
        if (expireTime - currentTime <= MILLIS_MINUTE_TEN) {
            refreshToken(loginUser);
        }
    }

    /**
     * 刷新令牌有效期
     *
     * @param loginUser 登录信息
     */
    public void refreshToken(LoginUser loginUser) {
        loginUser.setLoginTime(System.currentTimeMillis());
        loginUser.setExpireTime(loginUser.getLoginTime() + appProperties.token().expireTime() * MILLIS_MINUTE);
        cacheManager.getCache(CacheConstant.CACHE_LOGIN_TOKEN_KEY).put(loginUser.getToken(), JSON.toJSON(loginUser));
    }

    /**
     * 设置用户代理信息
     *
     * @param loginUser 登录信息
     */
    public void setUserAgent(LoginUser loginUser) {
        ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        UserAgent userAgent = UserAgent.parseUserAgentString(requestAttributes.getRequest().getHeader("User-Agent"));
        String ip = IpUtils.getIpAddr();
        loginUser.setIpaddr(ip);
        loginUser.setLoginLocation(AddressUtils.getRealAddressByIP(ip));
        loginUser.setBrowser(userAgent.getBrowser().getName());
        loginUser.setOs(userAgent.getOperatingSystem().getName());
    }

    /**
     * 从数据声明生成令牌
     *
     * @param claims 数据声明
     * @return 令牌
     */
    private String createToken(Map<String, String> claims) {
        JWTCreator.Builder builder = JWT.create();
        claims.forEach(builder::withClaim);
        return builder.sign(Algorithm.HMAC256(appProperties.token().secret()));
    }

    /**
     * 从令牌中获取数据声明
     *
     * @param token 令牌
     * @return 数据声明
     */
    private Map<String, Claim> parseToken(String token) {
        Algorithm algorithm = Algorithm.HMAC256(appProperties.token().secret());
        return JWT.require(algorithm).build().verify(token).getClaims();
    }

    /**
     * 获取请求token
     *
     * @param request
     * @return token
     */
    private String getToken(HttpServletRequest request) {
        String token = request.getHeader(appProperties.token().header());
        if (StringUtils.isNotEmpty(token) && token.startsWith(AppConstant.TOKEN_PREFIX)) {
            token = token.replace(AppConstant.TOKEN_PREFIX, "");
        }
        return token;
    }
}
