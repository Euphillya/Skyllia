plugins {
    id("java")
}

group = "fr.euphyllia.skyllia";
version = "1.0.3";

repositories {
    maven("https://repo.extendedclip.com/content/repositories/placeholderapi/")
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:1.21.3-R0.1-SNAPSHOT")
    compileOnly("me.clip:placeholderapi:2.11.6")
    compileOnly(project(":api"))
    compileOnly(project(":plugin"))
    compileOnly(project(":addons:SkylliaOre"))
    compileOnly(project(":addons:SkylliaBank"))
    implementation("com.github.ExcaliaSI:exp4j:e50bdd65e4")
    implementation("com.github.ben-manes.caffeine:caffeine:3.1.8")
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