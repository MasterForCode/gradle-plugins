package plugins;

import extensions.GenerateCoeExtension;
import tasks.GenerateCodeTask;
import org.gradle.api.NonNullApi;
import org.gradle.api.Plugin;
import org.gradle.api.Project;

/**
 * Created by wb on 2018/11/26.
 */
@NonNullApi
public class GenerateCodePlugin implements Plugin<Project> {
    @Override
    public void apply(Project project) {
        project.getExtensions().create("generateCode", GenerateCoeExtension.class);
        project.getTasks().create("generateCode", GenerateCodeTask.class);
    }
}