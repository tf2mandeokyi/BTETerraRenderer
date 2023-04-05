import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.gradle.api.DefaultTask
import org.gradle.api.artifacts.Configuration
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction

import java.util.jar.JarFile

class CustomConfigureShadowRelocation extends DefaultTask {
    @Input
    ShadowJar target

    @Input
    String prefix

    @Input
    List<Configuration> configurations

    @TaskAction
    void configureRelocation() {
        def packages = [] as Set<String>
        configurations.each { configuration ->
            configuration.files.each { jar ->
                JarFile jf = new JarFile(jar)
                jf.entries().each { entry ->
                    if (entry.name.endsWith(".class") && entry.name != "module-info.class") {
                        packages << entry.name[0..entry.name.lastIndexOf('/') - 1].replaceAll('/', '.')
                    }
                }
            }
        }
        packages.each { target.relocate(it, "${project.modGroup}.${project.modId}.dep.${it}") }
    }
}
