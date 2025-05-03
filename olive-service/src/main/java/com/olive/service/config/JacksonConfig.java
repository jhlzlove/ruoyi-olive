package com.olive.service.config;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import com.olive.base.utils.LocalDateUtil;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.TimeZone;

/**
 * 全局 jackson 配置
 *
 * @author jhlz
 * @version 1.0.0
 */
@Configuration
public class JacksonConfig implements Jackson2ObjectMapperBuilderCustomizer {

    @Override
    public void customize(Jackson2ObjectMapperBuilder builder) {
        JavaTimeModule module = new JavaTimeModule();
        module.addSerializer(LocalDate.class, new LocalDateSerializer(LocalDateUtil.DATE_FORMATTER));
        module.addDeserializer(LocalDate.class, new LocalDateDeserializer(LocalDateUtil.DATE_FORMATTER));
        module.addSerializer(LocalDateTime.class, new LocalDateTimeSerializer(LocalDateUtil.DATE_TIME_FORMATTER));
        module.addDeserializer(LocalDateTime.class, new LocalDateTimeDeserializer(LocalDateUtil.DATE_TIME_FORMATTER));
        builder.timeZone(TimeZone.getTimeZone("Asia/Shanghai"));
        // 忽略未知字段
        builder.featuresToEnable(JsonGenerator.Feature.IGNORE_UNKNOWN);
        builder.featuresToDisable(JsonParser.Feature.INCLUDE_SOURCE_IN_LOCATION);
    }
}
