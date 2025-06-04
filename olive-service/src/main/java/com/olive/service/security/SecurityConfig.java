package com.olive.service.security;

import com.olive.service.filter.JwtAuthenticationTokenFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.actuate.autoconfigure.security.servlet.EndpointRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.csrf.CsrfFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

/**
 * spring security配置
 *
 * @author Dftre
 */
@Configuration
@RequiredArgsConstructor
@EnableMethodSecurity(securedEnabled = true)
public class SecurityConfig {
    private final UserDetailsService userDetailsService;
    private final AuthenticationEntryPointImpl unauthorizedHandler;
    private final LogoutSuccessHandlerImpl logoutSuccessHandler;
    private final JwtAuthenticationTokenFilter jwtAuthenticationTokenFilter;

    /**
     * anyRequest | 匹配所有请求路径
     * access | SpringEl表达式结果为true时可以访问
     * anonymous | 匿名可以访问
     * denyAll | 用户不能访问
     * fullyAuthenticated | 用户完全认证可以访问（非remember-me下自动登录）
     * hasAnyAuthority | 如果有参数，参数表示权限，则其中任何一个权限可以访问
     * hasAnyRole | 如果有参数，参数表示角色，则其中任何一个角色可以访问
     * hasAuthority | 如果有参数，参数表示权限，则其权限可以访问
     * hasIpAddress | 如果有参数，参数表示IP地址，如果用户IP和参数匹配，则可以访问
     * hasRole | 如果有参数，参数表示角色，则其角色可以访问
     * permitAll | 用户可以任意访问
     * rememberMe | 允许通过remember-me登录的用户访问
     * authenticated | 用户登录后可访问
     */
    @Bean
    SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http
                // 使用 jwt 认证，禁用 CSRF
                .csrf(AbstractHttpConfigurer::disable)
                // 不需要 session
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                // 设置安全头
                .headers(header -> header
                        .cacheControl(HeadersConfigurer.CacheControlConfig::disable)
                        .frameOptions(HeadersConfigurer.FrameOptionsConfig::sameOrigin)
                )
                // 注解标记允许匿名访问的url
                .authorizeHttpRequests((requests) -> {
                    requests
                            // 放行 actuator
                            .requestMatchers(EndpointRequest.toAnyEndpoint()).permitAll()
                            // 登录 login、register、captchaImage 只允许匿名访问(即未登录状态)
                            .requestMatchers("/login", "/register", "/captchaImage")
                            .anonymous()
                            // 静态资源
                            .requestMatchers(
                                    // swagger
                                    "/swagger-ui/**",
                                    "/swagger-resources/**",
                                    "/v2/api-docs",
                                    "/v3/api-docs/**",
                                    "/webjars/**",
                                    // jimmer swagger 默认实现路径
                                    "/openapi.html",
                                    "/openapi.yml"
                            )
                            .permitAll()
                            .requestMatchers(
                                    // 测试接口完全放开
                                    "/test/**",
                                    // other
                                    "/biz/company/**",
                                    "/websocket/**"
                            )
                            .permitAll()
                            // 其它请求全部需要鉴权认证
                            .anyRequest().authenticated();
                })
                // 认证数据源
                .userDetailsService(userDetailsService)
                // 认证过滤器
                .addFilterBefore(jwtAuthenticationTokenFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterAfter(jwtAuthenticationTokenFilter, CsrfFilter.class)
                // 认证和权限不足处理，一般自己实现 AuthenticationEntryPoint、AccessDeniedHandler 两个接口然后注册到 security 中
                .exceptionHandling(exception -> exception.authenticationEntryPoint(unauthorizedHandler))
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .logout(logout -> logout.logoutUrl("/logout").logoutSuccessHandler(logoutSuccessHandler))
                .build();
    }

    /**
     * 使用默认的密码加密方式，使用该种方式必须在数据库密码中使用指定的 {xxx} 前缀
     *
     * @return 密码管理器
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

    /**
     * 暴露全局 AuthenticationManager（认证管理器）
     *
     * @return AuthenticationManager
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    /**
     * Spring Security 跨域配置
     *
     * @return CorsConfigurationSource
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of("*")); // 设置允许访问的来源
        configuration.setAllowedMethods(List.of("*")); // 设置允许的方法
        configuration.setAllowedHeaders(List.of("*")); // 设置允许的头
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
