plugins {
    id("java")
}

group = "fr.euphyllia.skylliaore";

repositories {
    maven("https://repo.oraxen.com/releases")
    maven("https://repo.nexomc.com/releases")
    maven("https://repo.extendedclip.com/content/repositories/placeholderapi/")
    maven("https://repo.momirealms.net/releases/")
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:1.20.6-R0.1-SNAPSHOT")

    compileOnly("io.th0rgal:oraxen:1.180.0")
    compileOnly("com.nexomc:nexo:1.16.1")
    compileOnly("me.clip:placeholderapi:2.11.6")
    compileOnly("net.momirealms:craft-engine-core:26.6")
    compileOnly("net.momirealms:craft-engine-bukkit:26.6")

    compileOnly(project(":database"))
    compileOnly(project(":api"))
    compileOnly(project(":plugin"))
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

tasks {
    compileJava {
        options.encoding = "UTF-8"
    }
}