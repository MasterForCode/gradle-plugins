package tasks;

import common.Const;
import extensions.HelpExtension;
import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.TaskAction;

/**
 * Created by wb on 2018/11/30.
 */
public class HelpTask extends DefaultTask {
    @TaskAction
    void help() {
        String id = ((HelpExtension) getProject().getExtensions().getByName(Const.EXTENSION_HELP_NAME)).getId();
        switch (id) {
            case Const.TASK_APIDOC_NAME:
                System.out.println("to be...");
                break;
            case Const.TASK_DELETE_NAME:
                System.out.println("like this: del {\n" +
                        "//    files 'CityController'\n" +
                        "    directories 'entity,  controller, service, dao'\n" +
                        "//    suffix 't'\n" +
                        "}");
                break;
            case Const.TASK_GENERATECODE_NAME:
                System.out.println("like this: generateCode {\n" +
                        "//    host '192.168.88.23'\n" +
                        "    port '3306'\n" +
                        "    dataBase 'world'\n" +
                        "    user 'root'\n" +
                        "    password 'wangbin_123'\n" +
                        "    entityPath 'entity'\n" +
                        "    controllerPath 'controller'\n" +
                        "    servicePath 'service'\n" +
                        "    serviceImplPath 'service.impl'\n" +
                        "    daoPath 'dao'\n" +
                        "}");
                break;
            case Const.TASK_HELP_NAME:
                System.out.println("like this: b_help{\n" +
                        "    id 'help' \n" +
                        "}");
                break;
        }
    }
}