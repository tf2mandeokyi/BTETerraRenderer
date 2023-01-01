package com.mndk.bteterrarenderer.loader;

import com.mndk.bteterrarenderer.BTETerraRenderer;
import lombok.Getter;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Objects;

public abstract class YamlLoader<T> {


    protected static final Yaml YAML = new Yaml();


    @Getter
    private File mapFilesDirectory;
    private final String folderName;
    private final String defaultYamlPath;
    public T result;


    protected YamlLoader(String folderName, String defaultYamlPath) {
        this.folderName = folderName;
        this.defaultYamlPath = defaultYamlPath;
    }


    public void refresh() throws Exception {

        result = loadDefault();

        if(mapFilesDirectory == null) return;

        if(!mapFilesDirectory.exists() && !mapFilesDirectory.mkdirs()) {
            throw new Exception("Map folder creation failed.");
        }
        File[] mapFiles = mapFilesDirectory.listFiles((dir, name) -> name.endsWith(".yml"));

        if (mapFiles != null) {
            for (File mapFile : mapFiles) {
                String name = mapFile.getName();
                try (FileReader fileReader = new FileReader((mapFile))) {
                    addToResult(result, load(name, fileReader));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }


    public void refresh(String modConfigDirectory) throws Exception {
        this.mapFilesDirectory = new File(
                modConfigDirectory + "/" + BTETerraRenderer.MODID + "/" + folderName
        );
        this.refresh();
    }


    protected T load(String fileName, Reader fileReader) {
        Map<String, Object> data = YAML.load(fileReader);
        return load(fileName, data);
    }


    private T loadDefault() {
        return load("default", new InputStreamReader(
                Objects.requireNonNull(YamlLoader.class.getClassLoader().getResourceAsStream(defaultYamlPath)),
                StandardCharsets.UTF_8
        ));
    }


    protected abstract T load(String fileName, Map<String, Object> data);
    protected abstract void addToResult(T originalT, T newT);

}
