package checking;

import history.VisibilityType;
import datatype.DataTypeFactory;
import history.HappenBeforeGraph;
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

public class VisearchChecker {
    protected String adt;
    private int threadNum = 8;
    private long averageState = 0;
    private boolean stateFilter = false;
    public boolean isStateFilter = false;

    public VisearchChecker(String adt, int threadNum) {
        this.adt = adt;
        this.threadNum = threadNum;
    }

    public VisearchChecker(String adt, int threadNum, boolean stateFilter) {
        this.adt = adt;
        this.threadNum = threadNum;
        this.stateFilter = stateFilter;
    }

    public boolean normalCheck(HappenBeforeGraph happenBeforeGraph, SearchConfiguration configuration) {
        RuleTable ruleTable = null;
        if (stateFilter) {
            ruleTable = extractRules(happenBeforeGraph, configuration.getVisibilityType());
            // if (ruleTable.size() > 0) {
            //     isStateFilter = true;
            // }
        }
        MinimalVisSearch vfs = new MinimalVisSearch(configuration, null);
        vfs.setRuleTable(ruleTable);
        vfs.init(happenBeforeGraph);
        boolean result = vfs.checkConsistency();
        // averageState += vfs.getStateExplored();
        return result;
    }

    public boolean multiThreadCheck(HappenBeforeGraph happenBeforeGraph, SearchConfiguration configuration) {
        if (threadNum == 1) {
            return normalCheck(happenBeforeGraph, configuration);
        }

        // System.out.println(happenBeforeGraph.toString());
        RuleTable ruleTable = null;
        if (stateFilter) {
            ruleTable = extractRules(happenBeforeGraph, configuration.getVisibilityType());
            // if (ruleTable.size() == 0) {
            //     isStateFilter = false;
            // }
        }

        SearchConfiguration subConfiguration = new SearchConfiguration.Builder()
                                                                .setVisibilityType(configuration.getVisibilityType())
                                                                .setAdt(configuration.getAdt())
                                                                .setEnableIncompatibleRelation(false)
                                                                .setEnableOutputSchedule(false)
                                                                .setEnablePrickOperation(false)
                                                                .setFindAllAbstractExecution(false)
                                                                .setVisibilityLimit(-1)
                                                                .setQueueLimit(32)
                                                                .setSearchMode(1)
                                                                .setSearchLimit(-1)
                                                                .build();
        MinimalVisSearch subVfs = new MinimalVisSearch(subConfiguration, null);
//        subVfs.setRuleTable(ruleTable);
        subVfs.init(happenBeforeGraph);
        boolean result = subVfs.checkConsistency();
        if (subVfs.isExit()) {
            // averageState += subVfs.getStateExplored();
            return result;
        }
        List<SearchState> states = subVfs.getAllSearchState();
        ThreadPoolSearch threadPoolSearch = new ThreadPoolSearch(null, happenBeforeGraph, configuration, threadNum);
        threadPoolSearch.setRuleTable(ruleTable);
        //MultiThreadSearch multiThreadSearch = new MultiThreadSearch(happenBeforeGraph, configuration, threadNum);
        //multiThreadSearch.setRuleTable(ruleTable);
        //result = multiThreadSearch.startSearch(states);
        try {
            result = threadPoolSearch.startSearch(states);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        //averageState += multiThreadSearch.getStateExplored();
        return result;
    }

    public long getAverageState() {
        return averageState;
    }

    protected RuleTable extractRules(HappenBeforeGraph happenBeforeGraph, VisibilityType visibilityType) {
        RuleTable ruleTable = new HBGPreprocessor().extractRules(happenBeforeGraph, adt, visibilityType);
        return ruleTable;
    }

    protected void removeDummyOperations(HappenBeforeGraph happenBeforeGraph) {
        new HBGPreprocessor().preprocess(happenBeforeGraph, adt);
    }

    protected HappenBeforeGraph load(String filename) {
        VisearchTraceFileLoader rp = new VisearchTraceFileLoader();
        HappenBeforeGraph happenBeforeGraph = rp.generateProgram(filename, DataTypeFactory.getInstance().getDataType(adt)).generateHappenBeforeGraph();
        return happenBeforeGraph;
    }

    protected synchronized void outputResult(String filename, List<SearchState> results) {
        try {
            ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(filename));
            oos.writeObject(results);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void readResult(String filename) {
        try {
            ObjectInputStream ois = new ObjectInputStream(new FileInputStream(filename));
            List<SearchState> results = (List<SearchState>) ois.readObject();
            System.out.println(results.get(0).toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void testDataSet(String filepath, VisibilityType visibilityType) throws Exception {
        File baseFile = new File(filepath);
        if (baseFile.isFile() || !baseFile.exists()) {
            throw new FileNotFoundException();
        }
        File[] files = baseFile.listFiles();
        int i = 0;
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        System.out.println("Starting " + df.format(new Date()));
        for (File file : files) {
            i++;
            if (i % 1000 == 0) {
                System.out.println(i);
            }
            Boolean result = testTrace(file.toString(),  visibilityType);
            if (!result) {
                System.out.println(file.toString() + ":" + result);
            }
        }
        System.out.println("Finishing " + df.format(new Date()));
    }

    public void testDataSet(List<String> dataset, VisibilityType visibilityType) throws Exception {
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        System.out.println("Starting " + df.format(new Date()));
        for (String file : dataset) {
            Boolean result = testTrace(file, visibilityType);
            System.out.println(file + ":" + result);
        }
        System.out.println("Finishing " + df.format(new Date()));
    }

    public void measureDataSet(List<String> dataset) throws Exception {
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        System.out.println("Starting " + df.format(new Date()));
        for (String file : dataset) {
            String result = measureVisibility(file);
            System.out.println(file + ":" + result);
        }
        System.out.println("Finishing " + df.format(new Date()));
    }

    public void measureDataSet(String filepath) throws Exception {
        File baseFile = new File(filepath);
        if (baseFile.isFile() || !baseFile.exists()) {
            throw new FileNotFoundException();
        }
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
            String result = measureVisibility(file.toString());
            if (!result.equals("COMPLETE")) {
                System.out.println(i + ":" + file + ":" + result);
            }
            if (i % 10000 == 0) {
                System.out.println("Processing " + df.format(new Date()));
            }
        }
        System.out.println("Finishing " + df.format(new Date()));
    }

    public boolean testTrace(String filename, VisibilityType visibilityType) throws Exception {
        SearchConfiguration configuration = new SearchConfiguration.Builder()
                .setAdt(adt)
                .setEnableIncompatibleRelation(false)
                .setEnablePrickOperation(false)
                .setEnableOutputSchedule(false)
                .setVisibilityType(visibilityType)
                .setFindAllAbstractExecution(false)
                .build();

        HappenBeforeGraph happenBeforeGraph = load(filename);
        removeDummyOperations(happenBeforeGraph);
        return multiThreadCheck(happenBeforeGraph, configuration);
    }

    public boolean testTrace(HappenBeforeGraph happenBeforeGraph, VisibilityType visibilityType) throws Exception {
        SearchConfiguration configuration = new SearchConfiguration.Builder()
                .setAdt(adt)
                .setEnableIncompatibleRelation(false)
                .setEnablePrickOperation(false)
                .setEnableOutputSchedule(false)
                .setVisibilityType(visibilityType)
                .setFindAllAbstractExecution(false)
                .build();

        return multiThreadCheck(happenBeforeGraph, configuration);
    }

    public List<String> filter(String filename) throws Exception {
        BufferedReader br = new BufferedReader(new FileReader(new File(filename)));
        List<String> result = new LinkedList<String>();
        String str = null;
        while ((str = br.readLine()) != null) {
            if (str.endsWith(":false"))
                result.add(str.substring(0, str.lastIndexOf(':')));
        }
        return result;
    }

    public String measureVisibility(String filename) throws Exception {
        for (int i = 0; i < 6; i++) {
            boolean result = testTrace(filename, VisibilityType.values()[i]);
            if (result) {
                return VisibilityType.values()[i].name();
            }
        }
        return "undefined";
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
                if (res.getBoolean("measure")) {
                    System.out.println(checker.measureVisibility(filepath));
                } else {
                    System.out.println(checker.testTrace(filepath, VisibilityType.getVisibility(res.getString("vis"))));
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
