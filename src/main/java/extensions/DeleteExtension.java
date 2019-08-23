package extensions;

import lombok.Data;

import java.util.List;

/**
 * Created by wb on 2018/11/30.
 */
@Data
public class DeleteExtension {
    private String directories;
    private List<String> files;
    private String suffix;
}
