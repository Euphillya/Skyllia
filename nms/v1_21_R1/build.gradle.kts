plugins {
    id("java")
    id("io.papermc.paperweight.userdev")
}

repositories {
    maven("https://github.com/Euphillya/FoliaDevBundle/raw/gh-pages/")
}

dependencies {
    paperweight.foliaDevBundle("1.21.1-R0.1-SNAPSHOT")
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