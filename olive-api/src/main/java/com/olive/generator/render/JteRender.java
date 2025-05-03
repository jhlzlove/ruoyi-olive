package com.olive.generator.render;

import com.olive.generator.model.GeneratorModel;
import gg.jte.ContentType;
import gg.jte.TemplateEngine;
import gg.jte.output.FileOutput;
import gg.jte.resolve.DirectoryCodeResolver;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * @author jhlz
 * @version 0.0.1
 */
public class JteRender {

    public static void main(String[] args) throws IOException {
        // jte 模板路径
        String jtePath = "olive-api/src/main/resources/jte";
        // 生成文件的路径
        Path generatePath = Paths.get("generated/");
        Files.createDirectories(generatePath);

        DirectoryCodeResolver resolver = new DirectoryCodeResolver(Path.of(jtePath));
        TemplateEngine templateEngine = TemplateEngine.create(resolver, ContentType.Plain);

        try (
                FileOutput fileOutput = new FileOutput(Paths.get(generatePath + "/TestService.java"))
        ){
            templateEngine.render("test.jte",
                    new GeneratorModel("com.olive", "Test"),
                    fileOutput
            );
        }
    }

}
