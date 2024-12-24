plugins {
    id("java")
}

group = "fr.euphyllia.skyllia";
version = "1.1";

dependencies {
    compileOnly("io.papermc.paper:paper-api:1.21.3-R0.1-SNAPSHOT")
    compileOnly(files("./libs_tmp/Insights-6.19.2.jar"))
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