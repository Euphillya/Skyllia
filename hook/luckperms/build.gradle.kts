plugins {
    id("java")
}

group = "fr.euphyllia.skyllia.hook.luckperms"

dependencies {
    compileOnly("io.papermc.paper:paper-api:1.20.6-R0.1-SNAPSHOT")
    compileOnly(project(":api"))

    compileOnly("net.luckperms:api:5.4")
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}
