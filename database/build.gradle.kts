plugins {
    id("java")
}
group = "fr.euphyllia.skyllia";
version = "1.3";

java {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}

tasks {
    compileJava {
        options.encoding = "UTF-8"
    }
}