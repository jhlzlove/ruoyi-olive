package com.olive.base.util;

import gg.jte.ContentType;
import gg.jte.TemplateEngine;
import gg.jte.output.StringOutput;
import gg.jte.resolve.DirectoryCodeResolver;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;

/**
 * @author jhlz
 * @version 0.0.1
 */
public class JteTest {
    /**
     * example: jte——test
     */
    @Test
    public void jte_test() {
        DirectoryCodeResolver resolver = new DirectoryCodeResolver(Path.of("src/test/resources/jte"));
        TemplateEngine templateEngine = TemplateEngine.create(resolver, ContentType.Plain);
        templateEngine.render("test.jte", "hello", new StringOutput());
    }
}
