plugins {
    id("java")
    id("io.papermc.paperweight.userdev")
}

//  Could not find me.lucko:spark-paper:1.10.105-SNAPSHOT.
configurations.all {
    exclude(group = "me.lucko", module = "spark-paper")
    exclude(group = "me.lucko", module = "spark-common")
    exclude(group = "me.lucko", module = "spark-api")
}


repositories {
    maven("https://github.com/Euphillya/FoliaDevBundle/raw/gh-pages/")
}

paperweight {
    paperweight.reobfArtifactConfiguration = io.papermc.paperweight.userdev.ReobfArtifactConfiguration.MOJANG_PRODUCTION
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