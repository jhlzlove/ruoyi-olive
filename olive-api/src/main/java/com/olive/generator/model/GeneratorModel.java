package com.olive.generator.model;

/**
 * @author jhlz
 * @version 0.0.1
 */
public record GeneratorModel(
        String packageName,
        String modelName
) {
    public static String lowerFirst(String source) {
        return source;
    }
}
