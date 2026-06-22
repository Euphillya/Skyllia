plugins {
    id("java")
}

group = "fr.euphyllia.skyllia.hook.luckperms"

repositories {
    maven {
        url = uri("https://repo.codemc.io/repository/maven-public/")
    }
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:1.20.6-R0.1-SNAPSHOT")
    compileOnly(project(":api"))

    compileOnly("com.ghostchu:quickshop-api:6.2.0.11")
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}
