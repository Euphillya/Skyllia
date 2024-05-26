plugins {
    id("java")
    id("io.papermc.paperweight.userdev")
}

dependencies {
    paperweight.foliaDevBundle("1.20.1-R0.1-SNAPSHOT")
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