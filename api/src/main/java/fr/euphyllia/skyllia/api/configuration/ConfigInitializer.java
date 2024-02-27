package fr.euphyllia.skyllia.api.configuration;

import java.io.File;

@FunctionalInterface
public interface ConfigInitializer {
    void initConfig(File configFile) throws Exception;
}
