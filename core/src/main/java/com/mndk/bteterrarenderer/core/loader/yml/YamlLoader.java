package com.mndk.bteterrarenderer.core.loader.yml;

import com.mndk.bteterrarenderer.core.BTETerraRenderer;
import com.mndk.bteterrarenderer.core.util.Loggers;
import lombok.Getter;
import lombok.Setter;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Objects;

@Getter
public abstract class YamlLoader<F, T> {

    private File filesDirectory;
    private final String folderName;
    private final String defaultYamlPath;
    private final Class<F> fileClazz;
    @Getter @Setter
    protected T result;

    protected YamlLoader(String folderName, String defaultYamlPath, Class<F> fileClazz) {
        this.folderName = folderName;
        this.defaultYamlPath = defaultYamlPath;
        this.fileClazz = fileClazz;
    }

    public void refresh() {

        // Load default data
        try {
            this.result = loadDefault();
        } catch(IOException e) {
            Loggers.get(this).error("Error while parsing default file: " + defaultYamlPath, e);
            return;
        }

        // Check folder
        if(filesDirectory == null) return;
        if(!filesDirectory.exists() && !filesDirectory.mkdirs()) {
            Loggers.get(this).error("Folder" + folderName + " creation failed");
            return;
        }

        File[] files = filesDirectory.listFiles((dir, name) -> name.endsWith(".yml"));
        if (files == null) return;

        for (File file : files) {
            String name = file.getName();
            try (InputStreamReader fileReader = new InputStreamReader(Files.newInputStream(file.toPath()), StandardCharsets.UTF_8)) {
                addToResult(this.result, load(name, fileReader));
            } catch (Exception e) {
                Loggers.get(this).error("Error while parsing file: " + file, e);
            }
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
    protected abstract void addToResult(T originalT, T newT);
}
