package com.mndk.bteterrarenderer.core.loader;

import com.mndk.bteterrarenderer.core.BTETerraRendererConstants;
import lombok.Getter;
import lombok.Setter;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

@Getter
public abstract class YamlLoader<T> {

    private File mapFilesDirectory;
    private final String folderName;
    private final String defaultYamlPath;
    @Getter @Setter
    protected T result;

    protected YamlLoader(String folderName, String defaultYamlPath) {
        this.folderName = folderName;
        this.defaultYamlPath = defaultYamlPath;
    }

    public void refresh() throws Exception {

        this.result = loadDefault();

        if(mapFilesDirectory == null) return;
        if(!mapFilesDirectory.exists() && !mapFilesDirectory.mkdirs()) {
            throw new Exception("Map folder creation failed.");
        }
        File[] mapFiles = mapFilesDirectory.listFiles((dir, name) -> name.endsWith(".yml"));
        if (mapFiles == null) return;

        for (File mapFile : mapFiles) {
            String name = mapFile.getName();
            try (FileReader fileReader = new FileReader((mapFile))) {
                addToResult(this.result, load(name, fileReader));
            } catch (Exception e) {
                BTETerraRendererConstants.LOGGER.error("Error while parsing map file " + mapFile, e);
            }
        }
    }

    public void refresh(File modConfigDirectory) throws Exception {
        this.mapFilesDirectory = new File(modConfigDirectory, folderName);
        this.refresh();
    }

    private T loadDefault() throws IOException {
        return load("default", new InputStreamReader(
                Objects.requireNonNull(YamlLoader.class.getClassLoader().getResourceAsStream(defaultYamlPath)),
                StandardCharsets.UTF_8
        ));
    }

    protected abstract T load(String fileName, Reader fileReader) throws IOException;
    protected abstract void addToResult(T originalT, T newT);
}
