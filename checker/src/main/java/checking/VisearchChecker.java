package checking;

import datatype.*;
import history.VisibilityType;
import history.HappenBeforeGraph;
import history.loader.FileHistoryLoader;
import history.loader.PlainOperationTransformer;
import rule.RuleTable;
import history.loader.VisearchTraceFileLoader;
import validation.*;
import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.ArgumentParserException;
import net.sourceforge.argparse4j.inf.Namespace;

import java.util.LinkedList;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import static net.sourceforge.argparse4j.impl.Arguments.*;

public class VisearchChecker extends Checker {
    public VisearchChecker(DataTypeCreator creator) {
        super(creator);
    }

    public VisearchChecker(DataTypeCreator creator, int threadNum) {
        super(creator, threadNum);
    }

    public VisearchChecker(DataTypeCreator creator, int threadNum, boolean enablePruning) {
        super(creator, threadNum, enablePruning);
    }

    public VisearchChecker(String datatype) {
        super(datatype);
    }

    public VisearchChecker(String datatype, int threadNum) {
        super(datatype, threadNum);
    }

    public VisearchChecker(String datatype, int threadNum, boolean enablePruning) {
        super(datatype, threadNum, enablePruning);
    }

    public void testDataSet(String filepath, VisibilityType visibilityType) throws Exception {
        File baseFile = new File(filepath);
        if (baseFile.isFile() || !baseFile.exists()) {
            throw new FileNotFoundException();
        }
        FileHistoryLoader loader = new VisearchTraceFileLoader(datatype);
        File[] files = baseFile.listFiles();
        int i = 0;
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        System.out.println("Starting " + df.format(new Date()));
        for (File file : files) {
            i++;
            if (i % 1000 == 0) {
                System.out.println(i);
            }
            HappenBeforeGraph happenBeforeGraph = loader.generateProgram(file.toString()).generateHappenBeforeGraph();
            boolean result = checkSingleTrace(happenBeforeGraph, visibilityType);
            if (!result) {
                System.out.println(file.toString() + ":" + result);
            }
        }
        System.out.println("Finishing " + df.format(new Date()));
    }

    public void testDataSet(List<String> dataset, VisibilityType visibilityType) throws Exception {
        FileHistoryLoader loader = new VisearchTraceFileLoader(datatype);
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        System.out.println("Starting " + df.format(new Date()));
        for (String file : dataset) {
            HappenBeforeGraph happenBeforeGraph = loader.generateProgram(file).generateHappenBeforeGraph();
            boolean result = checkSingleTrace(happenBeforeGraph, visibilityType);
            System.out.println(file + ":" + result);
        }
        System.out.println("Finishing " + df.format(new Date()));
    }

    public void measureDataSet(List<String> dataset) throws Exception {
        FileHistoryLoader loader = new VisearchTraceFileLoader(datatype);
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        System.out.println("Starting " + df.format(new Date()));
        for (String file : dataset) {
            HappenBeforeGraph happenBeforeGraph = loader.generateProgram(file).generateHappenBeforeGraph();
            String result = measureSingleTrace(happenBeforeGraph);
            System.out.println(file + ":" + result);
        }
        System.out.println("Finishing " + df.format(new Date()));
    }

    public void measureDataSet(String filepath) throws Exception {
        File baseFile = new File(filepath);
        if (baseFile.isFile() || !baseFile.exists()) {
            throw new FileNotFoundException();
        }
        FileHistoryLoader loader = new VisearchTraceFileLoader(datatype);
        File[] files = baseFile.listFiles();
        int i = 0;
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        System.out.println("Starting " + df.format(new Date()));
        for (File file : files) {
            i++;
            // if (i <= 60000) {
            //     continue;
            // }
            // if (file.toString().equals("../../pq-f/result/rwf_rpq_default_1636085387554633971.trc")) {
            //     continue;
            // }
            HappenBeforeGraph happenBeforeGraph = loader.generateProgram(file.toString()).generateHappenBeforeGraph();
            String result = measureSingleTrace(happenBeforeGraph);
            if (!result.equals("COMPLETE")) {
                System.out.println(i + ":" + file + ":" + result);
            }
            if (i % 10000 == 0) {
                System.out.println("Processing " + df.format(new Date()));
            }
        }
        System.out.println("Finishing " + df.format(new Date()));
    }

    public static void main(String[] args) throws Exception {
        ArgumentParser parser = ArgumentParsers
                .newFor("vs")
                .build()
                .defaultHelp(true)
                .description(
                        "ViSearch: A measurement framework for replicated data type on Vis-Ar weak consistency");
        parser.addArgument("-t", "--type").help(". Data type for checking")
                .type(String.class)
                .dest("type");
        parser.addArgument("--disable-pruning").help(". Disable pruning")
                .dest("pruning")
                .action(storeFalse());
        parser.addArgument("-f", "--filepath").help(". File path to check")
                .type(String.class)
                .dest("filepath");
        parser.addArgument("-p", "--parallel").help(". Number of parallel threads")
                .type(Integer.class)
                .dest("parallel")
                .setDefault(16);
        parser.addArgument("-v", "--vis").help(". Visibility Level")
                .type(String.class)
                .dest("vis")
                .setDefault("complete");
        parser.addArgument("--unset-measure").help(". Disable measure")
                .dest("measure")
                .action(storeFalse());
        parser.addArgument("--unset-dataset").help(". Disable checking for data set")
                .dest("dataset")
                .action(storeFalse());
        Namespace res;
        try {
            res = parser.parseArgs(args);
            System.out.println(res);
            
            String dataType = res.getString("type");
            String filepath = res.getString("filepath");
            int threadNum = res.getInt("parallel");
            boolean pruning = true;
            pruning = res.getBoolean("pruning");
            VisearchChecker checker = new VisearchChecker(dataType, threadNum, pruning);
            if (res.getBoolean("dataset")) {
                if (res.getBoolean("measure")) {
                    checker.measureDataSet(filepath);
                } else {
                    checker.testDataSet(filepath, VisibilityType.getVisibility(res.getString("vis")));
                }
            } else {
                FileHistoryLoader loader = new VisearchTraceFileLoader(dataType);
                HappenBeforeGraph happenBeforeGraph = loader.generateProgram(filepath).generateHappenBeforeGraph();
                if (res.getBoolean("measure")) {
                    System.out.println(checker.measureSingleTrace(happenBeforeGraph));
                } else {
                    System.out.println(checker.checkSingleTrace(happenBeforeGraph, VisibilityType.getVisibility(res.getString("vis"))));
                }
            }
            System.out.println(res);
            System.out.println("Exit");
        } catch (ArgumentParserException e) {
            parser.handleError(e);
            System.exit(1);
        }
        return;
    }
}
