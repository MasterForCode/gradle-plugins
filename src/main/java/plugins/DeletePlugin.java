package plugins;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import tasks.DeleteTask;

/**
 * Created by wb on 2018/11/28.
 */
public class DeletePlugin implements Plugin<Project> {
    @Override
    public void apply(Project project) {
        project.getTasks().create("delete", DeleteTask.class);
    }
}