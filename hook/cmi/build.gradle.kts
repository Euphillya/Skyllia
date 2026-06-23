plugins {
    id("java")
}
group = "fr.euphyllia.skyllia.hook.essentialsx"
version = "2.3"


repositories {
    mavenCentral()
}
dependencies {
    compileOnly("io.papermc.paper:paper-api:1.20.6-R0.1-SNAPSHOT") { isTransitive = false }
    compileOnly("net.kyori:adventure-text-minimessage:4.25.0")
    compileOnly(project(":api"))

    compileOnly("com.github.Zrips:CMI-API:9.8.6.4")
    compileOnly("com.github.Zrips:CMILib:1.5.9.6")
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}
