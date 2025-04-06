package com.olive.service.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.StreamReadFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.introspect.AnnotatedMember;
import com.fasterxml.jackson.databind.introspect.JacksonAnnotationIntrospector;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import com.olive.base.utils.LocalDateUtil;
import org.babyfish.jimmer.jackson.ImmutableModule;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

/**
 * jackson 封装的工具类
 */
public class JSON {
    private static final ObjectMapper mapper;

    static {
        mapper = new ObjectMapper();
        DateTimeFormatter dateTimeFormatter = LocalDateUtil.DATE_TIME_FORMATTER;
        DateTimeFormatter dateFormatter = LocalDateUtil.DATE_FORMATTER;
        JavaTimeModule module = new JavaTimeModule();
        module.addSerializer(LocalDate.class, new LocalDateSerializer(dateFormatter));
        module.addDeserializer(LocalDate.class, new LocalDateDeserializer(dateFormatter));
        module.addSerializer(LocalDateTime.class, new LocalDateTimeSerializer(dateTimeFormatter));
        module.addDeserializer(LocalDateTime.class, new LocalDateTimeDeserializer(dateTimeFormatter));
        mapper.setTimeZone(TimeZone.getTimeZone("Asia/Shanghai"));
        mapper.registerModule(module);
        mapper.registerModules(module, new ImmutableModule());
        mapper.disable(StreamReadFeature.INCLUDE_SOURCE_IN_LOCATION.mappedFeature());
    }

    public static String toJSON(Object obj) {
        try {
            return mapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    /**
     * 添加过滤属性
     * @param obj       需要转换的对象
     * @param filter    过滤的字符数组
     * @return          json 字符串
     */
    public static String toJSON(Object obj, String... filter) {
        try {
            mapper.setAnnotationIntrospector(new JacksonAnnotationIntrospector() {
                @Override
                public boolean hasIgnoreMarker(final AnnotatedMember m) {
                    List<String> exclusions = Arrays.asList(filter);
                    return exclusions.contains(m.getName()) || super.hasIgnoreMarker(m);
                }
            });
            return mapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    public static Map<String, Object> toMap(String json) {
        return toMap(json, Object.class);
    }

    public static <T> Map<String, T> toMap(String json, Class<T> clazz) {
        try {
            return mapper.readValue(json, mapper.getTypeFactory().constructMapType(Map.class, String.class, clazz));
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    public static <T> List<T> toList(String json, Class<T> clazz) {
        try {
            return mapper.readValue(json, mapper.getTypeFactory().constructCollectionType(List.class, clazz));
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    /**
     * 读取 json 字符串转为对象
     * @param json  json 字符串
     * @param clazz 对象类
     * @return  指定对象类型
     * @param <T>   泛型参数
     */
    public static <T> T toObj(String json, Class<T> clazz) {
        try {
            return mapper.readValue(json, clazz);
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage());
        }
    }
}
