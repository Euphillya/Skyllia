package fr.euphyllia.skylliagenerator;

import io.papermc.paper.plugin.loader.PluginClasspathBuilder;
import io.papermc.paper.plugin.loader.PluginLoader;
import io.papermc.paper.plugin.loader.library.impl.MavenLibraryResolver;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.graph.Dependency;
import org.eclipse.aether.repository.RemoteRepository;

public final class SkylliaGeneratorLoader implements PluginLoader {
    @Override
    public void classloader(PluginClasspathBuilder classpathBuilder) {
        MavenLibraryResolver resolver = new MavenLibraryResolver();
        resolver.addRepository(new RemoteRepository.Builder("central", "default", "https://repo.papermc.io/repository/maven-public/").build());

        resolver.addDependency(new Dependency(new DefaultArtifact("com.electronwill.night-config:toml:3.8.3"), null));

        classpathBuilder.addLibrary(resolver);
    }
}
