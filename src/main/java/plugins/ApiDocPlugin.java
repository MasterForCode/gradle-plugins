package plugins;

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
        project.getExtensions().create("apiDoc", ApiDocExtension.class);
        project.getTasks().create("apiDoc", ApiDocTask.class);
    }
}