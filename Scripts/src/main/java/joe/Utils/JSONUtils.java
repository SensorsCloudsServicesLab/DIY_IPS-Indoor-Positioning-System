package joe.Utils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Collectors;

public class JSONUtils {

    public static JSONArray loadJSONArray(Path path) {
        JSONArray array = new JSONArray();
        try {
            String content = Files.lines(path).collect(Collectors.joining());
            array = new JSONArray(content);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return array;
    }

    public static JSONObject loadJSONObject(Path path) {
        JSONObject object = new JSONObject();
        try {
            String content = Files.lines(path).collect(Collectors.joining());
            object = new JSONObject(content);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return object;
    }

    public static void writeFile(Path path, String content) {
        try {
            Files.write(path, content.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
