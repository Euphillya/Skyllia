plugins {
    id("java")
}

group = "fr.euphyllia.skyllia.hook.canvas"

repositories {
    maven {
        name = "Canvas"
        url = uri("https://maven.canvasmc.io/snapshots")
    }
}

dependencies {
    compileOnly("io.canvasmc.canvas:canvas-api:1.21.11-R0.1-SNAPSHOT")
}

java {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}
