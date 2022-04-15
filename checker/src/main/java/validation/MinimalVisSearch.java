package validation;

import datatype.AbstractDataType;
import datatype.DataTypeFactory;
import datatype.OperationTypes;
import history.HBGNode;
import history.HappenBeforeGraph;
import history.LinVisibility;
import history.Linearization;
import rule.RuleTable;

import java.util.*;

public class MinimalVisSearch {
    private SearchStatePriorityQueue stateQueue;
    private Deque<SearchState> stateDeque = new LinkedList<>();
    private static HappenBeforeGraph happenBeforeGraph;
    private RuleTable ruleTable = null;
    private SearchConfiguration configuration;
    private int stateExplored = 0;
    private HashMap<HBGNode, Integer> prickOperationCounter = new HashMap<>();
    private int readOperationFailLimit = 30;
    private List<SearchState> results = new ArrayList<>();
    private volatile boolean exit = false;
    private volatile boolean finish = false;
    private MultiSearchCoordinator coordinator;
    private Random random = new Random();
    private int loopNum;

    public MinimalVisSearch(SearchConfiguration configuration, MultiSearchCoordinator coordinator) {
        this.configuration = configuration;
        this.coordinator = coordinator;
        SearchState.visibilityType = configuration.getVisibilityType();
        loopNum = 1000 + random.nextInt(200) - 100;
    }

    public void init(HappenBeforeGraph happenBeforeGraph) {
        SearchState.happenBeforeGraph = happenBeforeGraph;
        MinimalVisSearch.happenBeforeGraph = happenBeforeGraph;
        SearchState startState = new SearchState();
        startState.getLinearization().addFront(happenBeforeGraph.getStartNodes());

//        stateQueue = new SearchStatePriorityQueue(configuration.getSearchMode());
//        for (SearchState newState : startState.linExtent()) {
//            stateQueue.offer(newState);
//        }

        for (SearchState newState : startState.linExtent()) {
            stateDeque.offerFirst(newState);
        }
    }

    public void init(HappenBeforeGraph happenBeforeGraph, SearchState initState) {
        SearchState.happenBeforeGraph = happenBeforeGraph;
        MinimalVisSearch.happenBeforeGraph = happenBeforeGraph;

//        stateQueue = new SearchStatePriorityQueue(configuration.getSearchMode());
//        stateQueue.offer(initState);

        stateDeque.offerFirst(initState);
    }

    public void init(HappenBeforeGraph happenBeforeGraph, List<SearchState> initStates) {
        SearchState.happenBeforeGraph = happenBeforeGraph;
        MinimalVisSearch.happenBeforeGraph = happenBeforeGraph;
        stateQueue = new SearchStatePriorityQueue(configuration.getSearchMode());
//        for (SearchState state : initStates) {
//            stateQueue.offer(state);
//        }

        for (SearchState state : initStates) {
            stateDeque.offerFirst(state);
        }
    }

    public boolean checkConsistency() {
        AbstractDataType adt = new DataTypeFactory().getDataType(configuration.getAdt());
        while (!stateDeque.isEmpty() && !exit
                && (configuration.getQueueLimit() == -1 || stateDeque.size() < configuration.getQueueLimit())) {
            SearchState state = stateDeque.pollFirst();
//            SearchState state = stateQueue.poll();
            List<HBGNode> subset = null;
            while ((subset = state.nextVisibility(ruleTable)) != null && !exit) {
                stateExplored++;
                if (stateExplored > loopNum && coordinator != null) {
                    stateExplored = 0;
                    loopNum = 1000 + random.nextInt(200) - 100;
                    if (stateDeque.size() > 1) {
                        List<SearchState> stateList = new LinkedList<>();
                        for (int i = 0; i < stateDeque.size() / 2; i++) {
                            stateList.add(stateDeque.pollLast());
                        }
                        MinimalVisSearch newSearch = new MinimalVisSearch(configuration, coordinator);
                        newSearch.init(happenBeforeGraph, stateList);
                        try {
                            if (!coordinator.loadShare(newSearch)) {
                                for (SearchState searchState : stateList) {
                                    stateDeque.offerLast(searchState);
                                }
                            }
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
                if (executeCheck(adt, state)) {
                    if (state.isComplete()) {
                        results.add((SearchState) state.clone());
                        if (!configuration.isFindAllAbstractExecution()) {
                            exit = true;
                            finish = true;
                            return true;
                        }
                    }
                    state.pruneVisibility(subset);
                    List<SearchState> list =state.linExtent(ruleTable);
//                    stateQueue.offer(state);
                    if (configuration.getSearchMode() == 0) {
                        stateDeque.offerFirst(state);
                    } else {
                        stateDeque.offerLast(state);
                    }
                    for (SearchState newState : list) {
//                        stateQueue.offer(newState);
                        if (configuration.getSearchMode() == 0) {
                            stateDeque.offerFirst(newState);
                        } else {
                            stateDeque.offerLast(newState);
                        }
                    }
                    break;
               } else {
                    handlePrickOperation(state);
               }
            }
        }
        //System.out.println(stateExplored);
        finish = true;
        return false;
    }

    public List<SearchState> getAllSearchState() {
        List<SearchState> states = new ArrayList<>();
//        while (!stateQueue.isEmpty()) {
//            states.add(stateQueue.poll());
//        }
        while (!stateDeque.isEmpty()) {
            states.add(stateDeque.pollFirst());
        }
        return states;
    }

    private boolean executeCheck(AbstractDataType adt, SearchState searchState) {
        boolean excuteResult = crdtExecute(adt, searchState);
        if (configuration.isEnableOutputSchedule()) {
            HBGNode lastOperation = searchState.getLinearization().getLast();
            if (searchState.getLinearization().size() % 10 == 0) {
                System.out.println(Thread.currentThread().getName() + ":" + lastOperation.toString() + " + " + searchState.getLinearization().size() + "/" + happenBeforeGraph.size() + "--" + searchState.getQueryOperationSize());
            }
        }
        return excuteResult;
    }

    private boolean crdtExecute(AbstractDataType adt, SearchState searchState) {
        Linearization lin = searchState.getLinearization();
        LinVisibility visibility = searchState.getVisibility();
        try {
            HBGNode lastNode = lin.getLast();
            if (lastNode.getInvocation().getOperationType() == OperationTypes.OPERATION_TYPE.UPDATE) {
                return true;
            } else if (lastNode.getInvocation().getOperationType() == OperationTypes.OPERATION_TYPE.QUERY) {
                Set<HBGNode> vis = visibility.getNodeVisibility(lastNode);
                for (int i = 0; i < lin.size() - 1; i++) {
                    HBGNode node = lin.get(i);
                    if (node.getInvocation().getOperationType() == OperationTypes.OPERATION_TYPE.UPDATE && vis.contains(node)) {
                        adt.excute(node.getInvocation());
                    }
                }
                String ret = adt.excute(lastNode.getInvocation());
                adt.reset();
                if (lastNode.getInvocation().getRetValue().equals(ret)) {
                    return true;
                } else {
//                    System.out.println(lastNode.getInvocation().toString());
                    return false;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    private void handlePrickOperation(SearchState state) {
        if (!configuration.isEnablePrickOperation()) {
            return;
        }
        HBGNode prickOperation = state.getLinearization().getLast();
        if (!prickOperationCounter.containsKey(prickOperation)) {
            prickOperationCounter.put(prickOperation, 1);
        } else {
            Integer failTimes = prickOperationCounter.get(prickOperation);
            if (failTimes == -1) {
                prickOperationCounter.remove(prickOperation);
                return;
            }
            prickOperationCounter.put(prickOperation, failTimes + 1);
            if (failTimes > readOperationFailLimit) {
                System.out.println(state.getLinearization().size() + ": " + "FAIL" + ":" + Integer.toString(failTimes) + " " + prickOperation);
                prickOperationCounter.put(prickOperation, -1);
            }
        }
    }

    public void setRuleTable(RuleTable ruleTable) {
        this.ruleTable = ruleTable;
    }

    public List<SearchState> getResults() {
        return results;
    }

    public void stopSearch() {
        exit = true;
        while (!finish) {
            ;
        }
    }

    public boolean isExit() {
        return exit;
    }

    public boolean isFinish() {
        return finish;
    }

    public int getStateExplored() {
        return stateExplored;
    }
}
