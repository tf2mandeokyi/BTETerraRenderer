import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.gradle.api.DefaultTask
import org.gradle.api.Task
import org.gradle.api.artifacts.Configuration
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction

import java.util.jar.JarFile

/**
 * A modification of {@link com.github.jengelman.gradle.plugins.shadow.tasks.ConfigureShadowRelocation}
 * to make configurations editable
 */
class CustomConfigureShadowRelocation extends DefaultTask {

    @Input
    ShadowJar target

    @Input
    String prefix

    // This part makes the difference from the original; it makes configurations editable
    @Input
    List<Configuration> configurations

    @TaskAction
    void configureRelocation() {
        def packages = [] as Set<String>
        def moduleInfoFiles = [] as Set<String>
        configurations.each { configuration ->
            configuration.files.each { jar ->
                JarFile jf = new JarFile(jar)
                jf.entries().each { entry ->
                    if (entry.name.endsWith("module-info.class")) { // Exclude module-info class(es)
                        moduleInfoFiles << entry.name
                    }
                    else if (entry.name.endsWith(".class")) {
                        packages << entry.name[0..entry.name.lastIndexOf('/') - 1].replaceAll('/', '.')
                    }
                }
                jf.close()
            }
        }
        moduleInfoFiles.each {
            target.exclude(it)
        }
        packages.each {
            target.relocate(it, "${prefix}.${it}")
        }
    }

    static String taskName(Task task) {
        return "configureRelocation${task.name.capitalize()}"
    }

}
