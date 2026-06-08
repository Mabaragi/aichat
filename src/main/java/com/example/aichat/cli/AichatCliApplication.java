package com.example.aichat.cli;

import com.example.aichat.AichatApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;

public final class AichatCliApplication {

    private AichatCliApplication() {
    }

    public static void main(String[] args) {
        System.exit(run(args));
    }

    public static int run(String[] args) {
        try (ConfigurableApplicationContext context = startContext(args)) {
            return context.getBean(CliRunner.class).run(args);
        }
    }

    static ConfigurableApplicationContext startContext(String[] args) {
        return new SpringApplicationBuilder(AichatApplication.class)
                .web(WebApplicationType.NONE)
                .run(args);
    }
}
