package tasks;

import common.Const;
import extensions.DeleteExtension;
import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.TaskAction;
import org.gradle.internal.impldep.org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by wb on 2018/11/28.
 */
public class DeleteTask extends DefaultTask {

    private final File rootDir = getProject().getRootDir();

    @TaskAction
    void delete() {
        DeleteExtension deleteExtension = (DeleteExtension) (getProject().getExtensions().findByName(Const.EXTENSION_DELETE_NAME));
        if (deleteExtension == null) {
            throw new RuntimeException("must configuration be_del");
        }
        String directories = deleteExtension.getDirectories();
        if (directories != null) {
            List<String> stringList = splitAndTrim(directories);
            for (String str : stringList) {
                List<File> fileList = new ArrayList<>();
                getDirectory(rootDir, str, fileList);
                fileList.forEach(this::deleteDirectory);
            }

        }
        List<String> files = deleteExtension.getFiles();
        String suffix = deleteExtension.getSuffix();
        List<String> fileNameList = new ArrayList<>();
        if (files != null) {
            for (String str : files) {
                if (suffix != null) {
                    fileNameList.add(str + "." + suffix);
                } else {
                    fileNameList.add(str + ".java");
                }
            }
            for (String str : fileNameList) {
                List<File> fileList = new ArrayList<>();
                getFile(rootDir, str, fileList);
                fileList.forEach(File::delete);
            }
        }


    }

    private void deleteDirectory(@NotNull File file) {
        if (file.isDirectory()) {
            File[] files = file.listFiles();
            if (files != null) {
                for (File file1 : files) {
                    if (file1.isFile()) {
                        boolean flag = file1.delete();
                        if (!flag) {
                            System.out.println(file.getAbsoluteFile() + "下的" + file1.getName() + "不能删除");
                        }
                    } else {
                        deleteDirectory(file1);
                    }
                }
            }
        } else {
            throw new RuntimeException(file.getName() + " is not a directory");
        }
        file.delete();
    }

    private void getDirectory(File rootFile, String directory, List<File> directoryList) {
        File[] subFile = rootFile.listFiles(File::isDirectory);
        if (subFile != null) {
            for (File file : subFile) {
                if (file.getName().equalsIgnoreCase(directory)) {
                    directoryList.add(file);
                } else {
                    getDirectory(file, directory, directoryList);
                }
            }
        }
    }

    private void getFile(@NotNull File rootFile, @NotNull String fileName, List<File> fileList) {
        if (rootFile.isDirectory()) {
            File[] files = rootFile.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isFile() && file.getName().equalsIgnoreCase(fileName)) {
                        fileList.add(file);
                    } else if (file.isDirectory()) {
                        getFile(file, fileName, fileList);
                    }
                }
            }
        } else {
            throw new RuntimeException("must directory");
        }
    }

    private List<String> splitAndTrim(String str) {
        List<String> stringList = Arrays.asList(str.split(","));
        return stringList.stream().map(String::trim).collect(Collectors.toList());
    }
}
