package plugins;

import org.gradle.api.Plugin;
import org.gradle.api.Project;

/**
 * Created by wb on 2018/11/28.
 */
public class HelpPlugin implements Plugin<Project> {
    @Override
    public void apply(Project project) {
        project.getTasks().create("help").dependsOn("apiDoc").dependsOn("generateCode").dependsOn("uploadArchives").dependsOn("delete");
    }
}