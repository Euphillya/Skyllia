plugins {
    id("java")
    id("io.papermc.paperweight.userdev")
}

dependencies {
    paperweight.paperDevBundle("26.2.build.+")
    compileOnly(project(":nms:v1_21_R7"))
    compileOnly(project(":api"))
}

tasks {
    compileJava {
        options.encoding = "UTF-8"
    }
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(25))
    }
}

configurations.all {
    exclude(group = "net.kyori", module = "adventure-text-serializer-ansi")
}
