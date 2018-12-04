package plugins;

import common.Const;
import extensions.HelpExtension;
import org.gradle.api.NonNullApi;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import tasks.HelpTask;

/**
 * Created by wb on 2018/11/28.
 */
@NonNullApi
public class HelpPlugin implements Plugin<Project> {
    @Override
    public void apply(Project project) {
        project.getExtensions().create(Const.EXTENSION_HELP_NAME, HelpExtension.class);
        project.getTasks().create(Const.TASK_HELP_NAME, HelpTask.class).setGroup(Const.TASK_GROUP);
    }
}