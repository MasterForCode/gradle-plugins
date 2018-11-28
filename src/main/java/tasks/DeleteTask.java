package tasks;

import org.gradle.api.DefaultTask;
import org.gradle.api.Project;
import org.gradle.api.tasks.TaskAction;

import java.io.File;

/**
 * Created by wb on 2018/11/28.
 */
public class DeleteTask extends DefaultTask {
    @TaskAction
    void delete() {
        String rootPath = getProject().getRootDir().getAbsolutePath() + "/src/main/java";
        String controllerPath = rootPath + File.separator + "controller";
        String servicePath = rootPath + File.separator + "service";
        String serviceImplPath = rootPath + File.separator + "service" + File.separator + "impl";
        String daoPath = rootPath + File.separator + "dao";
        String entityPath = rootPath + File.separator + "entity";
        File controllerDirectory = new File(controllerPath);
        File serviceDirectory = new File(servicePath);
        File serviceImplDirectory = new File(serviceImplPath);
        File daoDirectory = new File(daoPath);
        File entityDirectory = new File(entityPath);
        deleteFile(controllerDirectory);
        deleteFile(serviceImplDirectory);
        deleteFile(serviceDirectory);
        deleteFile(daoDirectory);
        deleteFile(entityDirectory);

    }

    private void  deleteFile(File file) {
        if (file.exists()) {
            File[] files = file.listFiles();
            for (int i = 0; i < files.length; i++) {
                files[i].delete();
            }
            file.delete();
        }
    }
}