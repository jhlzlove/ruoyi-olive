package com.olive.base.util;

import gg.jte.ContentType;
import gg.jte.TemplateEngine;
import gg.jte.output.FileOutput;
import gg.jte.resolve.DirectoryCodeResolver;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * @author jhlz
 * @version 0.0.1
 */
public class JteTest {
    /**
     * example: jte——test
     */
    @Test
    public void jte_test() throws IOException {
        DirectoryCodeResolver resolver = new DirectoryCodeResolver(Path.of("src/test/resources/jte"));
        TemplateEngine templateEngine = TemplateEngine.create(resolver, ContentType.Plain);

        Path outputPath = Paths.get("jte-classes/gg/jte/generated/111/MengService.java");

        // outputPath
        Files.createDirectories(outputPath.getParent());

        try (FileOutput fileOutput = new FileOutput(outputPath)){
            templateEngine.render("test.jte",
                    new Model("Test", "com.olive"),
                    fileOutput
            );
        }

    }

}
