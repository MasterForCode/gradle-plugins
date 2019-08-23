package plugins;

import common.Const;
import extensions.GenerateCoeExtension;
import org.gradle.api.NonNullApi;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import tasks.GenerateCodeTask;

/**
 * Created by wb on 2018/11/26.
 */
@NonNullApi
public class GenerateCodePlugin implements Plugin<Project> {
    @Override
    public void apply(Project project) {
        project.getExtensions().create(Const.EXTENSION_GENERATECODE_NAME, GenerateCoeExtension.class);
        project.getTasks().create(Const.TASK_GENERATECODE_NAME, GenerateCodeTask.class).setGroup(Const.TASK_GROUP);
    }
}
