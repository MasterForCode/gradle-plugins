package tasks;

import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.TaskAction;

/**
 * Created by wb on 2018/11/30.
 */
public class HelpTask extends DefaultTask {
    @TaskAction
    void help() {
        System.out.println("Message...........");
    }
}