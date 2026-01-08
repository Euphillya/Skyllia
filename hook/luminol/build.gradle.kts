plugins {
    id("java")
}

group = "fr.euphyllia.skyllia.hook.luminol"

repositories {
    maven {
        name = "Luminol"
        url = uri("https://repo.menthamc.org/repository/maven-public/")
    }
}

dependencies {
    compileOnly(project(":api"))
    compileOnly("me.earthme.luminol:luminol-api:1.21.11-R0.1-SNAPSHOT")
}

java {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}
