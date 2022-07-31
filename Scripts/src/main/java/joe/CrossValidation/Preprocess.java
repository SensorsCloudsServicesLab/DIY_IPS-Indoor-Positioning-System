package joe.CrossValidation;

import joe.DistributionFile.DistributionGenerator;
import joe.Model.Position;
import joe.Utils.JSONUtils;
import org.json.JSONArray;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class Preprocess {

    /**
     * Create directories
     */
    public static void initDirectories() {
        for (int i = 1; i <= CrossValidationConstant.FOLD; i++) {
            String name = String.valueOf(i);
            Path p = CrossValidationConstant.preBase.resolve(name);
            try {
                Files.createDirectories(p);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Split data into 10 folds by default
     *
     * @param path
     * @return
     */
    public static List<List<Object>> createFolds(Path path) {
        JSONArray jsonArray = JSONUtils.loadJSONArray(path);
        int length = jsonArray.length();
        int foldLength = length / CrossValidationConstant.FOLD;
        List<Object> jsonList = jsonArray.toList();
        // shuffle the list randomly
        Collections.shuffle(jsonList, new Random(6));
        // split the list to 10 lists with equal size
        List<List<Object>> foldList = new ArrayList<>();
        for (int i = 0; foldList.size() < 10 ; i += foldLength) {
            if (foldList.size() == 9) {
                foldList.add(jsonList.subList(i, length));
                break;
            }
            foldList.add(jsonList.subList(i, i + foldLength));
        }
        return foldList;
    }

    /**
     * Generate 10 distribution.json and corresponding test.json by default
     *
     * @param path
     */
    public static void preprocessLocalRecords(Path path) {
        List<List<Object>> foldList = createFolds(path);

        for (int i = 0; i < foldList.size(); i++) {
            // test data
            List<Object> testData = foldList.get(0);
            JSONArray testJSONArray = new JSONArray(testData);
            String dir = String.valueOf(i + 1);
            Path dirPath = CrossValidationConstant.preBase.resolve(dir);
            Path testDataFilePath = dirPath.resolve(CrossValidationConstant.testDataFileName);
            // save test data to data/pre/dir folder
            JSONUtils.writeFile(testDataFilePath, testJSONArray.toString());

            // training data
            List<Object> trainData = new ArrayList<>();
            for (int j = 0; j < foldList.size(); j++) {
                if (j != i) {
                    trainData.addAll(foldList.get(j));
                }
            }
            JSONArray trainJSONData = new JSONArray(trainData);
            // generate distributions.json using training data
            List<Map<String, Map<Position, List<Double>>>> RSSIData = DistributionGenerator.getRSSIData(trainJSONData);
            List<Map<String, Map<Position, List<Double>>>> RSSIDistributions = DistributionGenerator.processDistributions(RSSIData);
            Path distributionFilePath = dirPath.resolve(CrossValidationConstant.distributionFileName);
            DistributionGenerator.saveDataJSON(RSSIDistributions, distributionFilePath);
        }

    }

    public static void main(String[] args) {
        initDirectories();
        Path localRecordsPath = Paths.get("data", "local_records.json");
        preprocessLocalRecords(localRecordsPath);
    }
}
