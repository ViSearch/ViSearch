package validation;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import history.HappenBeforeGraph;
import rule.RuleTable;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class ThreadPoolSearch {
    private SearchConfiguration configuration;
    private static HappenBeforeGraph happenBeforeGraph;
    private int threadNum = 16;
    private RuleTable ruleTable = null;
    private ThreadPoolExecutor pool;

    public ThreadPoolSearch(HappenBeforeGraph happenBeforeGraph, SearchConfiguration configuration, int threadNum) {
        ThreadPoolSearch.happenBeforeGraph = happenBeforeGraph;
        this.configuration = configuration;
        this.threadNum = threadNum;
        ThreadFactory namedThreadFactory = new ThreadFactoryBuilder().setNameFormat("Visearch-pool-%d").build();
        pool = new ThreadPoolExecutor(threadNum / 2, threadNum, 3000L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>(1024), namedThreadFactory, new ThreadPoolExecutor.AbortPolicy());
    }

    public boolean startSearch(List<SearchState> startStates) throws InterruptedException {
        Semaphore semaphore = new Semaphore(0);
        MultiSearchCoodinator coodinator = new MultiSearchCoodinator(pool, semaphore);
        int stateNum = startStates.size();
//        System.out.println("stateNum: " + stateNum);
        for (int i = 0; i < threadNum; i++) {
            List<SearchState> initStates = new LinkedList<>();
            for (int k = i; k < stateNum; k = k + threadNum) {
                initStates.add(startStates.get(k));
            }
            //System.out.println("initStates: " + initStates.size());
            MinimalVisSearch visSearch = new MinimalVisSearch(configuration);
            visSearch.init(happenBeforeGraph, initStates);
            visSearch.setRuleTable(ruleTable);
            pool.execute(new SearchTask(visSearch, coodinator));
        }
        semaphore.acquire(threadNum);
        pool.shutdownNow();
        while (!pool.isShutdown()) {
            ;
        }
        return coodinator.getResult();
    }

    public void setRuleTable(RuleTable ruleTable) {
        this.ruleTable = ruleTable;
    }
}

class MultiSearchCoodinator {
    private int threadNum;
    private ThreadPoolExecutor pool;
    private volatile boolean result = false;
    private Semaphore semaphore;
    private AtomicInteger idleThreadNum = new AtomicInteger(0);

    public MultiSearchCoodinator(ThreadPoolExecutor pool, Semaphore semaphore) {
        this.pool = pool;
        this.threadNum = pool.getMaximumPoolSize();
        this.semaphore = semaphore;
    }

    public boolean loadShare(MinimalVisSearch visSearch) {
        if (idleThreadNum.get() > 0) {
            synchronized (MultiSearchCoodinator.class) {
                if (idleThreadNum.get() > 0) {
                    pool.execute(new SearchTask(visSearch, this));
                    idleThreadNum.decrementAndGet();
                    return true;
                }
            }
        }
        return false;
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

//    public void setResult(boolean result) {
//        this.result = result;
//    }

    public boolean getResult() {
        return result;
    }
}

class SearchTask implements Runnable {
    private MinimalVisSearch visSearch;
    private MultiSearchCoodinator coodinator;

    public SearchTask(MinimalVisSearch visSearch, MultiSearchCoodinator coodinator) {
        this.visSearch = visSearch;
        this.coodinator = coodinator;
    }

    public void run() {
        boolean result = visSearch.checkConsistency();
        if (result) {
            coodinator.find();
        } else {
            coodinator.finish();
        }
    }
}
