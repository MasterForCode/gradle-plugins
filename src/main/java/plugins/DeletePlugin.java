package plugins;

import common.Const;
import extensions.DeleteExtension;
import org.gradle.api.NonNullApi;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import tasks.DeleteTask;

/**
 * Created by wb on 2018/11/28.
 */
@NonNullApi
public class DeletePlugin implements Plugin<Project> {
    @Override
    public void apply(Project project) {
        project.getExtensions().create(Const.EXTENSION_DELETE_NAME, DeleteExtension.class);
        project.getTasks().create(Const.TASK_DELETE_NAME, DeleteTask.class).setGroup(Const.TASK_GROUP);
    }
}