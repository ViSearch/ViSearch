package validation;

import checking.VisearchChecker;
import history.HappenBeforeGraph;
import rule.RuleTable;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class MultiThreadSearch {
    private SearchConfiguration configuration;
    private static HappenBeforeGraph happenBeforeGraph;
    private List<SearchThread> searchThreads = new ArrayList<>();
    private RuleTable ruleTable = null;
    private int searchThreadNum = 16;
    private int stateExplored = 0;

    public MultiThreadSearch(HappenBeforeGraph happenBeforeGraph, SearchConfiguration configuration, int threadNum) {
        MultiThreadSearch.happenBeforeGraph = happenBeforeGraph;
        this.configuration = configuration;
        this.searchThreadNum = threadNum;
    }


    public boolean startSearch(List<SearchState> startStates) {
        int stateNum = startStates.size();
//        System.out.println(stateNum);
        SearchLock searchLock = new SearchLock(searchThreadNum);
        for (int i = 0; i < searchThreadNum; i++) {
            List<SearchState> initStates = new LinkedList<>();
            for (int k = i; k < stateNum; k = k + searchThreadNum) {
                initStates.add(startStates.get(k));
            }
            MinimalVisSearch visSearch = new MinimalVisSearch(configuration, null);
            visSearch.init(happenBeforeGraph, initStates);
            visSearch.setRuleTable(ruleTable);
            searchThreads.add(new SearchThread(visSearch, searchLock));
        }

        for (SearchThread search : searchThreads) {
            new Thread(search).start();
        }
        searchLock.hold();
        for (SearchThread search : searchThreads) {
            search.stop();
        }
        stateExplored = searchLock.getStateExplored();
        return searchLock.getResult();
    }

    public void setRuleTable(RuleTable ruleTable) {
        this.ruleTable = ruleTable;
    }

    public int getStateExplored() {
        return stateExplored;
    }
}

class SearchLock {
    private final int size;
    private int stateExplored = 0;
    private AtomicBoolean satisfication = new AtomicBoolean(false);
    private AtomicInteger finished = new AtomicInteger(0);

    public SearchLock(int size) {
        this.size = size;
    }

    public void hold() {
        while (!satisfication.get() && finished.get() < size) {
            ;
        }
    }

    public void finish() {
        finished.incrementAndGet();
    }

    public void find() {
        satisfication.set(true);
    }

    public boolean getResult() {
        return satisfication.get();
    }

    public void addStateExplored(int explored) {
        stateExplored += explored;
    }

    public int getStateExplored() {
        return stateExplored;
    }
}

class SearchThread implements Runnable {
    private MinimalVisSearch visSearch;
    private SearchLock searchLock;

    public SearchThread(MinimalVisSearch visSearch, SearchLock searchLock) {
        this.visSearch = visSearch;
        this.searchLock = searchLock;
    }

    public void run() {
        boolean result = visSearch.checkConsistency();
        if (result) {
            searchLock.find();
        }
        searchLock.finish();
    }

    public void stop() {
        visSearch.stopSearch();
//        System.out.println(visSearch.getStateExplored());
        searchLock.addStateExplored(visSearch.getStateExplored());
    }

    public boolean isExit() {
        return visSearch.isExit();
    }

    public List<SearchState> getResults() {
        return visSearch.getResults();
    }
}
