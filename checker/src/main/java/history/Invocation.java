package history;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.tuple.Pair;

public class Invocation {
    private String methodName;
    private List<Object> arguments;
    private List<Object> retValues;

    private boolean isQuery = false;

    private boolean isUpdate = false;
    private int id;
    private int threadId;
    private Pair<Integer, Integer> pairID;

    public Invocation() {
        arguments = new ArrayList<Object>();
        retValues = new ArrayList<>();
    }

    public void setRetValues(List<Object> retValues) {
        this.retValues = retValues;
    }

    public List<Object> getRetValues() {
        return retValues;
    }

    public void setMethodName(String name) {
        methodName = name;
    }

    public void addArguments(Object object) {
        arguments.add(object);
    }

    public void addRetValue(Object object) {
        retValues.add(object);
    }

    public String getMethodName() {
        return methodName;
    }

    public List<Object> getArguments() {
        return arguments;
    }

    public boolean isQuery() {
        return isQuery;
    }

    public boolean isUpdate() {
        return isUpdate;
    }

    public void setQuery(boolean query) {
        isQuery = query;
    }

    public void setUpdate(boolean update) {
        isUpdate = update;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getThreadId() {
        return threadId;
    }

    public void setThreadId(int threadId) {
        this.threadId = threadId;
    }

    public Pair<Integer, Integer> getPairID() {
        return pairID;
    }

    public void setPairID(Pair<Integer, Integer> pairID) {
        this.pairID = pairID;
    }

    @Override
    public int hashCode() {
        return id;
    }

    @Override
    public String toString() {
        return methodName + ":" + arguments + ":" + retValues;
    }
}


