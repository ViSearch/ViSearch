package validation;

import history.HappenBeforeGraph;
import rule.RuleTable;

import java.util.LinkedList;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class ThreadPoolSearch {
    private SearchConfiguration configuration;
    private static HappenBeforeGraph happenBeforeGraph;
    private int threadNum = 16;
    private RuleTable ruleTable = null;
    private MultiSearchCoordinator coordinator;
    private static int epoch = 0;

    // public ThreadPoolSearch(ThreadPoolExecutor pool, HappenBeforeGraph happenBeforeGraph, SearchConfiguration configuration, int threadNum) {
    //     ThreadPoolSearch.happenBeforeGraph = happenBeforeGraph;
    //     this.configuration = configuration;
    //     this.threadNum = threadNum;
    //     epoch++;
    //     coordinator = new MultiSearchCoordinator(pool);
    // }

    public ThreadPoolSearch(ThreadPoolExecutor pool, HappenBeforeGraph happenBeforeGraph, SearchConfiguration configuration, int threadNum) {
        ThreadPoolSearch.happenBeforeGraph = happenBeforeGraph;
        this.configuration = configuration;
        this.threadNum = threadNum;
        coordinator = new MultiSearchCoordinator(threadNum);
    }

    public boolean startSearch(List<SearchState> startStates) throws InterruptedException {
        int stateNum = startStates.size();
//        System.out.println("stateNum: " + stateNum);
        for (int i = 0; i < threadNum; i++) {
            List<SearchState> initStates = new LinkedList<>();
            for (int k = i; k < stateNum; k = k + threadNum) {
                initStates.add(startStates.get(k));
            }
            //System.out.println("initStates: " + initStates.size());
            MinimalVisSearch visSearch = new MinimalVisSearch(configuration, coordinator);
            visSearch.init(happenBeforeGraph, initStates);
            visSearch.setRuleTable(ruleTable);
            // coordinator.addSearch(visSearch);
            coordinator.execute(visSearch);
        }
        coordinator.await();
        coordinator.exiting = true;
        coordinator.shutdown();
        return coordinator.getResult();
    }



    public void setRuleTable(RuleTable ruleTable) {
        this.ruleTable = ruleTable;
    }
}

class MultiSearchCoordinator {
    private int threadNum;
    private int epoch;
    public volatile boolean exiting = false;
    private ArrayList<SearchTask> taskList;
    private volatile boolean result = false;
    private volatile boolean sharing = false;
    private Semaphore semaphore = new Semaphore(0);
    private AtomicInteger idleThreadNum = new AtomicInteger(0);
    //private ThreadPoolExecutor pool;
    private List<MinimalVisSearch> currentSearch = new LinkedList<>();

    // public MultiSearchCoordinator(ThreadPoolExecutor pool) {
    //     this.pool = pool;
    //     this.threadNum = pool.getMaximumPoolSize();
    //     taskList = new ArrayList<>();
    // }

    public MultiSearchCoordinator(int threadNum) {
        this.threadNum = threadNum;
        taskList = new ArrayList<>();
    }

    public boolean loadShare(MinimalVisSearch visSearch) throws InterruptedException {
        if (idleThreadNum.get() > 0 && !exiting) {
            synchronized (MultiSearchCoordinator.class) {
                if (idleThreadNum.get() > 0 && !exiting) {
                    idleThreadNum.decrementAndGet();
                    semaphore.acquire();
                    execute(visSearch);
                    return true;
                }
            }
        }
        return false;
    }

    public void addSearch(MinimalVisSearch search) {
        currentSearch.add(search);
    }

    public void finish() {
        //System.out.println("finish");
        idleThreadNum.incrementAndGet();
        semaphore.release();
    }

    public void find() {
        //System.out.println("find");
        result = true;
        //System.out.println(result);
        semaphore.release(threadNum);
    }

    public void shutdown() {
        exiting = true;
        synchronized (MultiSearchCoordinator.class) {
            exiting = true;
            int sz = taskList.size();
            for (int i = 0; i < sz; i++) {
                SearchTask task = taskList.get(i);
                if (!task.visSearch.isFinish()) {
                    task.visSearch.stopSearch();
                }
            }
            
        }
    }

    public void execute(MinimalVisSearch search) {
        SearchTask task = new SearchTask(search, this);
        taskList.add(task);
        new Thread(task).start();
    }

    public void await() throws InterruptedException {
        semaphore.acquire(threadNum);
    }

    public boolean getResult() {
        return result;
    }
}

class SearchTask implements Runnable {
    public MinimalVisSearch visSearch;
    private MultiSearchCoordinator coordinator;

    public SearchTask(MinimalVisSearch visSearch, MultiSearchCoordinator coordinator) {
        this.visSearch = visSearch;
        this.coordinator = coordinator;
    }

    public void run() {
        boolean result = visSearch.checkConsistency();
        if (result) {
            coordinator.find();
        } else {
            coordinator.finish();
        }
    }
}
