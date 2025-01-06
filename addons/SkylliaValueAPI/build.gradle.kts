plugins {
    id("java")
}

group = "fr.excaliamc.skyllia_value";
version = "1.1";

repositories {
    maven("https://repo.oraxen.com/releases")
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:1.21.3-R0.1-SNAPSHOT")
    compileOnly(project(":database"))
    compileOnly(project(":api"))
    compileOnly(project(":plugin"))
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