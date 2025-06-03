plugins {
    id("java")
    id("io.papermc.paperweight.userdev")
}

repositories {
    maven {
        name = "folia-inquistors-repo"
        url = uri("https://folia-inquisitors.github.io/FoliaDevBundle/")
    }
}

paperweight {
    paperweight.reobfArtifactConfiguration = io.papermc.paperweight.userdev.ReobfArtifactConfiguration.MOJANG_PRODUCTION
}

dependencies {
    paperweight.foliaDevBundle("1.21.5-R0.1-SNAPSHOT")
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