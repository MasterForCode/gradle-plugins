package plugins;

import common.Const;
import extensions.ApiDocExtension;
import extensions.GenerateCoeExtension;
import org.gradle.api.NonNullApi;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import tasks.ApiDocTask;

/**
 * Created by wb on 2018/11/28.
 */
@NonNullApi
public class ApiDocPlugin implements Plugin<Project> {
    @Override
    public void apply(Project project) {
        project.getExtensions().create(Const.EXTENSION_APIDOC_NAME, ApiDocExtension.class);
        project.getTasks().create(Const.TASK_APIDOC_NAME, ApiDocTask.class).setGroup(Const.TASK_GROUP);
    }
}