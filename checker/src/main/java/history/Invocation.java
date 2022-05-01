package history;

import datatype.OperationTypes.OPERATION_TYPE;

import com.alibaba.fastjson.annotation.JSONField;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.tuple.Pair;

public class Invocation {
    private String methodName;
    private List<Object> arguments;
    private List<Object> retValues;

    private int id;
    private int threadId;
    private Pair<Integer, Integer> pairID;
    private OPERATION_TYPE operationType = OPERATION_TYPE.QUERY;

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

    public String getMethodName() {
        return methodName;
    }

    public List<Object> getArguments() {
        return arguments;
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

    public OPERATION_TYPE getOperationType() {
        return operationType;
    }

    public void setOperationType(OPERATION_TYPE operationType) {
        this.operationType = operationType;
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


