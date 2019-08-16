package extensions;

import org.gradle.api.Incubating;

import java.util.List;

/**
 * Created by wb on 2018/11/30.
 */
@Incubating
public class DeleteExtension {
    private String directories;
    private List<String> files;
    private String suffix;

    public String getDirectories() {
        return directories;
    }

    public void setDirectories(String directories) {
        this.directories = directories;
    }

    public List<String> getFiles() {
        return files;
    }

    public void setFiles(List<String> files) {
        this.files = files;
    }

    public String getSuffix() {
        return suffix;
    }

    public void setSuffix(String suffix) {
        this.suffix = suffix;
    }
}
