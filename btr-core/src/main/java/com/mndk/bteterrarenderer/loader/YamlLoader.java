package com.mndk.bteterrarenderer.loader;

import com.mndk.bteterrarenderer.BTETerraRendererConstants;
import lombok.Getter;
import lombok.Setter;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

public abstract class YamlLoader<T> {


    @Getter
    private File mapFilesDirectory;
    private final String folderName;
    private final String defaultYamlPath;
    @Getter @Setter
    public T result;

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
                e.printStackTrace();
            }
        }
    }

    public void refresh(String modConfigDirectory) throws Exception {
        this.mapFilesDirectory = new File(
                modConfigDirectory + "/" + BTETerraRendererConstants.MODID + "/" + folderName
        );
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
