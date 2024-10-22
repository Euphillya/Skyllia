plugins {
    id("java")
    id("io.papermc.paperweight.userdev")
}

repositories {
    maven("https://github.com/Euphillya/FoliaDevBundle/raw/gh-pages/")
}

dependencies {
    paperweight.paperDevBundle("1.21.2-R0.1-SNAPSHOT")
    compileOnly(project(":nms:v1_20_R4"))
    compileOnly(project(":api"))
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
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}