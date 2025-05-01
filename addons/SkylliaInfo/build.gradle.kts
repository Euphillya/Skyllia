plugins {
    id("java")
}

group = "fr.euphyllia.skyllia";
version = "1.2";

repositories {
    maven("https://repo.oraxen.com/releases")
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:1.21.3-R0.1-SNAPSHOT")
    compileOnly(project(":database"))
    compileOnly(project(":api"))
    compileOnly(project(":plugin"))

    // Addons
    compileOnly(project(":addons:SkylliaOre"))
    compileOnly(project(":addons:SkylliaBank"))
}

java {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}

tasks {
    compileJava {
        options.encoding = "UTF-8"
    }
}