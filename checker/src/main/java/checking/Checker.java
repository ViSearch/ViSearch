package checking;

import datatype.*;
import history.HappenBeforeGraph;
import history.VisibilityType;
import history.loader.FileHistoryLoader;
import history.loader.VisearchTraceFileLoader;
import rule.RuleTable;
import validation.*;

import java.util.List;

public class Checker {
    protected String datatype;
    protected int threadNum;
    protected boolean enablePruning;
    public Checker(String datatype) {
        this.datatype = datatype;
        this.threadNum = 4;
        this.enablePruning = false;
    }

    public Checker(AbstractDataType datatype) {
        this.datatype = datatype.getClass().getSimpleName();
        DataTypeFactory.getInstance().addDataType(this.datatype, datatype.getClass());
        this.threadNum = 4;
        this.enablePruning = false;
    }

    public Checker(AbstractDataType datatype, int threadNum) {
        this.datatype = datatype.getClass().getSimpleName();
        DataTypeFactory.getInstance().addDataType(this.datatype, datatype.getClass());
        this.threadNum = threadNum;
        this.enablePruning = false;
    }

    public Checker(AbstractDataType datatype, int threadNum, boolean enablePruning) {
        this.datatype = datatype.getClass().getSimpleName();
        DataTypeFactory.getInstance().addDataType(this.datatype, datatype.getClass());
        this.threadNum = threadNum;
        this.enablePruning = enablePruning;
    }

    public Checker(String datatype, int threadNum) {
        this.datatype = datatype;
        this.threadNum = threadNum;
    }

    public Checker(String datatype, int threadNum, boolean enablePruning) {
        this.datatype = datatype;
        this.threadNum = threadNum;
        this.enablePruning = enablePruning;
    }

    protected RuleTable extractRules(HappenBeforeGraph happenBeforeGraph, VisibilityType visibilityType) {
        RuleTable ruleTable = new HBGPreprocessor().extractRules(happenBeforeGraph, datatype, visibilityType);
        return ruleTable;
    }

    protected void removeDummyOperations(HappenBeforeGraph happenBeforeGraph) {
        new HBGPreprocessor().preprocess(happenBeforeGraph, datatype);
    }

    public boolean serialCheck(HappenBeforeGraph happenBeforeGraph, SearchConfiguration configuration) {
        RuleTable ruleTable = null;
        if (enablePruning) {
            ruleTable = extractRules(happenBeforeGraph, configuration.getVisibilityType());
        }
        MinimalVisSearch vfs = new MinimalVisSearch(configuration, null);
        vfs.setRuleTable(ruleTable);
        vfs.init(happenBeforeGraph);
        return vfs.checkConsistency();
    }

    public boolean parallelCheck(HappenBeforeGraph happenBeforeGraph, SearchConfiguration configuration) {
        if (threadNum == 1) {
            return serialCheck(happenBeforeGraph, configuration);
        }
        RuleTable ruleTable = null;
        if (enablePruning) {
            ruleTable = extractRules(happenBeforeGraph, configuration.getVisibilityType());
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
        subVfs.init(happenBeforeGraph);
        boolean result = subVfs.checkConsistency();
        if (subVfs.isExit()) {
            return result;
        }
        List<SearchState> states = subVfs.getAllSearchState();
        ThreadPoolSearch threadPoolSearch = new ThreadPoolSearch(null, happenBeforeGraph, configuration, threadNum);
        threadPoolSearch.setRuleTable(ruleTable);
        try {
            result = threadPoolSearch.startSearch(states);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return result;
    }

    public boolean checkSingleTrace(HappenBeforeGraph happenBeforeGraph, VisibilityType visibilityType) {
        SearchConfiguration configuration = new SearchConfiguration.Builder()
                .setAdt(datatype)
                .setEnableIncompatibleRelation(false)
                .setEnablePrickOperation(false)
                .setEnableOutputSchedule(false)
                .setVisibilityType(visibilityType)
                .setFindAllAbstractExecution(false)
                .build();
        removeDummyOperations(happenBeforeGraph);
        return parallelCheck(happenBeforeGraph, configuration);
    }

    public String measureSingleTrace(HappenBeforeGraph happenBeforeGraph) {
        for (int i = 0; i < 6; i++) {
            boolean result = checkSingleTrace(happenBeforeGraph, VisibilityType.values()[i]);
            if (result) {
                return VisibilityType.values()[i].name();
            }
        }
        return "undefined";
    }
}
