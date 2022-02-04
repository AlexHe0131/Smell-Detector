package cmu.csdetector;

import cmu.csdetector.console.ToolParameters;
import cmu.csdetector.console.output.ObservableExclusionStrategy;
import cmu.csdetector.metrics.MethodMetricValueCollector;
import cmu.csdetector.metrics.TypeMetricValueCollector;
import cmu.csdetector.resources.Method;
import cmu.csdetector.resources.loader.JavaFilesFinder;
import cmu.csdetector.resources.loader.SourceFile;
import cmu.csdetector.resources.loader.SourceFilesLoader;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import cmu.csdetector.resources.Type;
import org.apache.commons.cli.ParseException;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class CodeSmellDetector {

    public static void main(String[] args) throws IOException {
        CodeSmellDetector instance = new CodeSmellDetector();

        instance.start(args);

    }

    private void start(String[] args) throws IOException {
        ToolParameters parameters = ToolParameters.getInstance();

        try {
            parameters.parse(args);
        } catch (ParseException exception) {
            System.out.println(exception.getMessage());
            parameters.printHelp();

            System.exit(-1);
        }

        System.out.println(new Date());

        List<String> sourcePaths = List.of(parameters.getValue(ToolParameters.SOURCE_FOLDER));
        List<Type> allTypes = this.loadAllType(sourcePaths);
        collectTypeMetrics(allTypes);

        saveSmellsFile(allTypes);

        System.out.println(new Date());
    }

    private List<Type> loadAllType(List<String> sourcePaths) throws IOException {
        List<Type> allTypes = new ArrayList<>();
        JavaFilesFinder sourceLoader = new JavaFilesFinder(sourcePaths);
        SourceFilesLoader compUnitLoader = new SourceFilesLoader(sourceLoader);
        List<SourceFile> sourceFiles = compUnitLoader.getLoadedSourceFiles();

        for(SourceFile sourceFile: sourceFiles) {
            allTypes.addAll(sourceFile.getTypes());
        }
        return allTypes;
    }

    private void collectTypeMetrics(List<Type> types) {
        for (Type type: types) {
            TypeMetricValueCollector typeCollector = new TypeMetricValueCollector();
            typeCollector.collect(type);

            this.collectMethodMetrics(type);
        }
    }

    private void collectMethodMetrics(Type type) {
        for (Method method: type.getMethods()) {
            MethodMetricValueCollector methodCollector = new MethodMetricValueCollector();
            methodCollector.collect(method);
        }
    }

    private void saveSmellsFile(List<Type> smellyTypes) throws IOException {
        ToolParameters parameters = ToolParameters.getInstance();
        File smellsFile = new File(parameters.getValue(ToolParameters.SMELLS_FILE));
        BufferedWriter writer = new BufferedWriter(new FileWriter(smellsFile));
        System.out.println("Saving smells file...");

        GsonBuilder builder = new GsonBuilder();
        builder.addSerializationExclusionStrategy(new ObservableExclusionStrategy());
        builder.disableHtmlEscaping();
        builder.setPrettyPrinting();
        builder.serializeNulls();

        Gson gson = builder.create();
        gson.toJson(smellyTypes, writer);
        writer.close();
    }
}
