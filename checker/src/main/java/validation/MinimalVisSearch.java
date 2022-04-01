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
//    private LinkedBlockingDeque<SearchState> stateQueue = new LinkedBlockingDeque<>();
    private static HappenBeforeGraph happenBeforeGraph;
    private RuleTable ruleTable = null;
    private SearchConfiguration configuration;
    private int stateExplored = 0;
    private HashMap<HBGNode, Integer> prickOperationCounter = new HashMap<>();
    private int readOperationFailLimit = 30;
    private List<SearchState> results = new ArrayList<>();
    private volatile boolean exit = false;

    public MinimalVisSearch(SearchConfiguration configuration) {
        this.configuration = configuration;
        SearchState.visibilityType = configuration.getVisibilityType();
    }

    public void init(HappenBeforeGraph happenBeforeGraph) {
        SearchState.happenBeforeGraph = happenBeforeGraph;
        MinimalVisSearch.happenBeforeGraph = happenBeforeGraph;
        SearchState startState = new SearchState();
        startState.getLinearization().addFront(happenBeforeGraph.getStartNodes());
        //stateQueue = new SearchStatePriorityQueue(configuration.getSearchMode());
        for (SearchState newState : startState.linExtent()) {
            //stateQueue.offer(newState);
            stateDeque.addFirst(newState);
        }
    }

    public void init(HappenBeforeGraph happenBeforeGraph, SearchState initState) {
        SearchState.happenBeforeGraph = happenBeforeGraph;
        MinimalVisSearch.happenBeforeGraph = happenBeforeGraph;
        //stateQueue = new SearchStatePriorityQueue(configuration.getSearchMode());
        //stateQueue.offer(initState);
        stateDeque.addFirst(initState);
    }

    public void init(HappenBeforeGraph happenBeforeGraph, List<SearchState> initStates) {
        SearchState.happenBeforeGraph = happenBeforeGraph;
        MinimalVisSearch.happenBeforeGraph = happenBeforeGraph;
       // stateQueue = new SearchStatePriorityQueue(configuration.getSearchMode());
        for (SearchState state : initStates)
//            stateQueue.offer(state);
            stateDeque.addFirst(state);
    }

    public boolean checkConsistency() {
        AbstractDataType adt = new DataTypeFactory().getDataType(configuration.getAdt());
        while (!stateDeque.isEmpty() && !exit
                && (configuration.getQueueLimit() == -1 || stateDeque.size() < configuration.getQueueLimit())) {
            SearchState state = stateDeque.pollFirst();
            List<HBGNode> subset = null;
            while ((subset = state.nextVisibility(ruleTable)) != null && !exit) {
                stateExplored++;
                if (executeCheck(adt, state)) {
                    if (state.isComplete()) {
//                        System.out.println(stateExplored);
//                        System.out.println(state.toString());
                        results.add((SearchState) state.clone());
//                        results.add(state);
                        if (!configuration.isFindAllAbstractExecution()) {
                            exit = true;
                            return true;
                        }
                    }
                    state.pruneVisibility(subset);
                    List<SearchState> list =state.linExtent(ruleTable);
                    //stateQueue.offer(state);
                    if (configuration.getSearchMode() == 0) {
                        stateDeque.addFirst(state);
                    } else {
                        stateDeque.addLast(state);
                    }
                    for (SearchState newState : list) {
                        //stateQueue.offer(newState);
                        if (configuration.getSearchMode() == 0) {
                            stateDeque.addFirst(state);
                        } else {
                            stateDeque.addLast(state);
                        }
                    }
                    break;
               } else {
                    handlePrickOperation(state);
               }
            }
        }
//        System.out.println(stateExplored);
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
    }

    public boolean isExit() {
        return exit;
    }

    public int getStateExplored() {
        return stateExplored;
    }
}
