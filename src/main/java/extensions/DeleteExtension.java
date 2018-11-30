package extensions;

import org.gradle.api.Incubating;

/**
 * Created by wb on 2018/11/30.
 */
@Incubating
public class DeleteExtension {
    private String directories;
    private String files;
    private String suffix;

    public String getDirectories() {
        return directories;
    }

    public void setDirectories(String directories) {
        this.directories = directories;
    }

    public String getFiles() {
        return files;
    }

    public void setFiles(String files) {
        this.files = files;
    }

    public String getSuffix() {
        return suffix;
    }

    public void setSuffix(String suffix) {
        this.suffix = suffix;
    }
}