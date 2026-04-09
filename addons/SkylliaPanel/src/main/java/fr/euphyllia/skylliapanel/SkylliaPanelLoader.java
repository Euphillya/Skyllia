package fr.euphyllia.skylliapanel;

import io.papermc.paper.plugin.loader.PluginClasspathBuilder;
import io.papermc.paper.plugin.loader.PluginLoader;
import io.papermc.paper.plugin.loader.library.impl.MavenLibraryResolver;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.graph.Dependency;
import org.eclipse.aether.repository.RemoteRepository;

public class SkylliaPanelLoader implements PluginLoader {
    @Override
    public void classloader(PluginClasspathBuilder classpathBuilder) {
        MavenLibraryResolver resolver = new MavenLibraryResolver();
        resolver.addRepository(new RemoteRepository.Builder("central", "default", "https://repo.papermc.io/repository/maven-public/").build());
        resolver.addRepository(new RemoteRepository.Builder("central", "default", "https://repo.euphyllia.moe/repository/maven-public/").build());

        // Maven Repository Dependencies
        resolver.addDependency(new Dependency(new DefaultArtifact("com.electronwill.night-config:toml:3.8.3"), null));

        resolver.addDependency(new Dependency(new DefaultArtifact("dev.triumphteam:triumph-gui-paper:3.1.13"), null));
        resolver.addDependency(new Dependency(new DefaultArtifact("dev.triumphteam:triumph-gui:3.1.13"), null));

        classpathBuilder.addLibrary(resolver);
    }
}
