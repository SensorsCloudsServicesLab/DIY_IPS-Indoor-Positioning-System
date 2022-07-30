package joe.CrossValidation;

import java.nio.file.Path;
import java.nio.file.Paths;

public class CrossValidationConstant {
    public static final Path preBase = Paths.get("data", "pre");
    public static final Path modelDataPath = Paths.get("data", "model");
    public static final String testDataFileName = "test.json";
    public static final String distributionFileName = "distributions.json";
    public static final int FOLD = 10;
}
