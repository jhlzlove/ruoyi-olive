package com.olive.service.aop.log;

import com.olive.base.util.LocalDateUtil;
import com.olive.service.util.ServletUtils;
import com.olive.model.*;
import com.olive.service.manager.AsyncManager;
import com.olive.service.manager.factory.AsyncFactory;
import com.olive.service.util.JSON;
import com.olive.service.util.ip.AddressUtils;
import com.olive.service.util.ip.IpUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.babyfish.jimmer.ImmutableObjects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.NamedThreadLocal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.validation.BindingResult;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;

import java.util.Collection;
import java.util.Map;
import java.util.Objects;

/**
 * 操作日志记录处理
 *
 * @author ruoyi
 */
@Aspect
@Component
public class LogAspect {
    private static final Logger log = LoggerFactory.getLogger(LogAspect.class);

    /**
     * 排除敏感属性字段
     */
    public static final String[] EXCLUDE_PROPERTIES = {"password", "oldPassword", "newPassword", "confirmPassword"};

    /**
     * 计算操作消耗时间
     */
    private static final ThreadLocal<Long> TIME_THREADLOCAL = new NamedThreadLocal<Long>("Cost Time");

    /**
     * 处理请求前执行
     */
    @Before(value = "@annotation(controllerLog)")
    public void boBefore(JoinPoint joinPoint, Log controllerLog) {
        TIME_THREADLOCAL.set(System.currentTimeMillis());
    }

    /**
     * 处理完请求后执行
     *
     * @param joinPoint 切点
     */
    @AfterReturning(pointcut = "@annotation(controllerLog)", returning = "jsonResult")
    public void doAfterReturning(JoinPoint joinPoint, Log controllerLog, Object jsonResult) {
        handleLog(joinPoint, controllerLog, null, jsonResult);
    }

    /**
     * 拦截异常操作
     *
     * @param joinPoint 切点
     * @param e         异常
     */
    @AfterThrowing(value = "@annotation(controllerLog)", throwing = "e")
    public void doAfterThrowing(JoinPoint joinPoint, Log controllerLog, Exception e) {
        handleLog(joinPoint, controllerLog, e, null);
    }

    protected void handleLog(final JoinPoint joinPoint, Log controllerLog, final Exception e, Object jsonResult) {
        try {
            // 获取当前的用户
            LoginUser loginUser = (LoginUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

            // *========数据库日志=========*//
            SysOperLog sysOperLog = SysOperLogDraft.$.produce(operLog -> {
                operLog.setStatus(0);
                // 请求的地址
                String ip = IpUtils.getIpAddr();
                operLog.setOperTime(LocalDateUtil.dateTime());
                operLog.setOperIp(ip);
                operLog.setOperLocation(AddressUtils.getRealAddressByIP(ip));
                ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
                operLog.setOperUrl(StringUtils.substring(requestAttributes.getRequest().getRequestURI(), 0, 255));
                if (loginUser != null) {
                    operLog.setOperName(loginUser.getUsername());
                    SysUser currentUser = loginUser.getUser();
                    ImmutableObjects.isLoaded(currentUser, SysUserProps.DEPT);
                    if (ImmutableObjects.isLoaded(currentUser, SysUserProps.DEPT) &&
                            Objects.nonNull(currentUser) &&
                            Objects.nonNull(currentUser.dept())) {
                        if (ImmutableObjects.isLoaded(currentUser.dept(), SysDeptProps.DEPT_NAME)) {
                            operLog.setDeptName(currentUser.dept().deptName());
                        }
                    }
                }

                if (e != null) {
                    operLog.setStatus(1);
                    operLog.setErrorMsg(StringUtils.substring(e.getMessage(), 0, 2000));
                }
                // 设置方法名称
                String className = joinPoint.getTarget().getClass().getName();
                String methodName = joinPoint.getSignature().getName();
                operLog.setMethod(className + "." + methodName + "()");
                // 设置请求方式
                // operLog.setRequestMethod(ServletUtils.getRequest().getMethod());
                // 处理设置注解上的参数
                getControllerMethodDescription(joinPoint, controllerLog, operLog, jsonResult);
                // 设置消耗时间
                operLog.setCostTime(System.currentTimeMillis() - TIME_THREADLOCAL.get());
            });

            // 保存数据库
            AsyncManager.me().execute(AsyncFactory.recordOper(sysOperLog));
        } catch (Exception exp) {
            // 记录本地异常日志
            log.error("异常信息:{}", exp.getMessage());
            exp.printStackTrace();
        } finally {
            TIME_THREADLOCAL.remove();
        }
    }

    /**
     * 获取注解中对方法的描述信息 用于Controller层注解
     *
     * @param log     日志
     * @param operLog 操作日志
     * @throws Exception
     */
    public void getControllerMethodDescription(JoinPoint joinPoint, Log log, SysOperLogDraft operLog, Object jsonResult) throws Exception {
        // 设置action动作
        operLog.setBusinessType(log.businessType().ordinal());
        // 设置标题
        operLog.setTitle(log.title());
        // 设置操作人类别
        operLog.setOperatorType(log.operatorType().ordinal());
        // 是否需要保存request，参数和值
        if (log.isSaveRequestData()) {
            // 获取参数的信息，传入到数据库中。
            setRequestValue(joinPoint, operLog, log.excludeParamNames());
        }
        // 是否需要保存response，参数和值
        if (log.isSaveResponseData() && Objects.nonNull(jsonResult)) {
            operLog.setJsonResult(StringUtils.substring(JSON.toJSON(jsonResult), 0, 2000));
        }
    }

    /**
     * 获取请求的参数，放到log中
     *
     * @param operLog 操作日志
     * @throws Exception 异常
     */
    private void setRequestValue(JoinPoint joinPoint, SysOperLogDraft operLog, String[] excludeParamNames) throws Exception {
        Map<?, ?> paramsMap = ServletUtils.getParamMap(ServletUtils.getRequest());
        String requestMethod = ServletUtils.getRequest().getMethod();
        operLog.setRequestMethod(requestMethod);
        operLog.setOperParam(StringUtils.substring(JSON.toJSON(paramsMap, ArrayUtils.addAll(EXCLUDE_PROPERTIES, excludeParamNames)), 0, 2000));
    }

    /**
     * 参数拼装
     */
    private String argsArrayToString(Object[] paramsArray, String[] excludeParamNames) {
        String params = "";
        if (paramsArray != null && paramsArray.length > 0) {
            for (Object o : paramsArray) {
                if (Objects.nonNull(o) && !isFilterObject(o)) {
                    try {
                        String jsonObj = JSON.toJSON(o, ArrayUtils.addAll(EXCLUDE_PROPERTIES, excludeParamNames));
                        params += jsonObj.toString() + " ";
                    } catch (Exception e) {
                    }
                }
            }
        }
        return params.trim();
    }

    /**
     * 判断是否需要过滤的对象。
     *
     * @param o 对象信息。
     * @return 如果是需要过滤的对象，则返回true；否则返回false。
     */
    @SuppressWarnings("rawtypes")
    public boolean isFilterObject(final Object o) {
        Class<?> clazz = o.getClass();
        if (clazz.isArray()) {
            return clazz.getComponentType().isAssignableFrom(MultipartFile.class);
        } else if (Collection.class.isAssignableFrom(clazz)) {
            Collection collection = (Collection) o;
            for (Object value : collection) {
                return value instanceof MultipartFile;
            }
        } else if (Map.class.isAssignableFrom(clazz)) {
            Map map = (Map) o;
            for (Object value : map.entrySet()) {
                Map.Entry entry = (Map.Entry) value;
                return entry.getValue() instanceof MultipartFile;
            }
        }
        return o instanceof MultipartFile || o instanceof HttpServletRequest || o instanceof HttpServletResponse
                || o instanceof BindingResult;
    }
}
