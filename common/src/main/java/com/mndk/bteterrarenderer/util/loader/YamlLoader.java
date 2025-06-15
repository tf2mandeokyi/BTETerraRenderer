package com.mndk.bteterrarenderer.util.loader;

import com.mndk.bteterrarenderer.BTETerraRenderer;
import com.mndk.bteterrarenderer.util.Loggers;
import com.mndk.bteterrarenderer.util.merge.MergeStrategy;
import lombok.Getter;
import lombok.Setter;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import java.util.stream.Stream;

@Getter
public abstract class YamlLoader<F, T> {

    private File filesDirectory;
    private final String folderName;
    private final String defaultYamlPath;
    private final Class<F> fileClazz;
    @Getter @Setter
    protected T result;
    private final MergeStrategy<T> mergeStrategy;

    protected YamlLoader(String folderName, String defaultYamlPath, Class<F> fileClazz, MergeStrategy<T> mergeStrategy) {
        this.folderName = folderName;
        this.defaultYamlPath = defaultYamlPath;
        this.fileClazz = fileClazz;
        this.mergeStrategy = mergeStrategy;
    }

    public void refresh() {

        // Load default data
        try { this.result = loadDefault(); }
        catch (IOException e) {
            Loggers.get(this).error("Error while parsing default file: {}", defaultYamlPath, e);
            return;
        }

        // Check folder
        if (filesDirectory == null) return;
        if (!filesDirectory.exists() && !filesDirectory.mkdirs()) {
            Loggers.get(this).error("Folder {} creation failed", folderName);
            return;
        }

        try (Stream<Path> paths = Files.walk(filesDirectory.toPath())) {
            paths.filter(Files::isRegularFile)
                    .filter(p -> p.toString().endsWith(".yml"))
                    .forEach(this::addYmlFileAsPath);
        } catch (IOException e) {
            Loggers.get(this).error("Error walking directory: {}", filesDirectory, e);
        }
    }

    private void addYmlFileAsPath(Path p) {
        String name = filesDirectory.toPath().relativize(p).toString();
        try (InputStreamReader fileReader = new InputStreamReader(Files.newInputStream(p), StandardCharsets.UTF_8)) {
            mergeStrategy.merge(this.result, load(name, fileReader));
        } catch (Exception e) {
            Loggers.get(this).error("Error parsing file: {}", p, e);
        }
    }

    public void refresh(File modConfigDirectory) {
        this.filesDirectory = new File(modConfigDirectory, folderName);
        this.refresh();
    }

    private T loadDefault() throws IOException {
        return load("default", new InputStreamReader(
                Objects.requireNonNull(YamlLoader.class.getClassLoader().getResourceAsStream(defaultYamlPath)),
                StandardCharsets.UTF_8
        ));
    }

    private T load(String fileName, Reader fileReader) throws IOException {
        F dataTransferObject = BTETerraRenderer.YAML_MAPPER.readValue(fileReader, this.fileClazz);
        return this.load(fileName, dataTransferObject);
    }

    protected abstract T load(String fileName, F f) throws IOException;
}
