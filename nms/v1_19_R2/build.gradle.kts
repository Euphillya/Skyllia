plugins {
    id("io.papermc.paperweight.userdev") version "1.6.3" // Check for new versions at https://plugins.gradle.org/plugin/io.papermc.paperweight.userdev
}

dependencies {
    paperweight.paperDevBundle("1.19.3-R0.1-SNAPSHOT")
    compileOnly(project(":api"))
    compileOnly("com.github.Euphillya:Energie:1.2.0")
}

tasks {
    assemble {
        dependsOn(reobfJar)
    }
    compileJava {
        options.encoding = "UTF-8"
    }
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}