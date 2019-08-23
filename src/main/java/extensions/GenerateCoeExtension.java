package extensions;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by wb on 2018/11/26.
 */
@Data
public class GenerateCoeExtension {
    private String host = "localhost";
    private String port = "3306";
    private String dataBase;
    private String user = "root";
    private String password = "root";
    private List<String> tableNames = new ArrayList<>();
    private String entityPath = "entity";
    private String controllerPath = "controller";
    private String servicePath = "service";
    private String serviceImplPath = "service.impl";
    private String daoPath = "dao";
}
