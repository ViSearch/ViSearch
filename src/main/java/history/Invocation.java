package history;

import datatype.OperationTypes.OPERATION_TYPE;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.annotation.JSONField;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.lang3.tuple.Pair;

public class Invocation implements Serializable {
    @JSONField(name = "METHOD NAME", ordinal = 1)
    private String methodName;
    @JSONField(name = "ARGUMENTS", ordinal = 2)
    private List<Object> arguments = new ArrayList<Object>();
    @JSONField(name = "RETVALUE", ordinal = 3)
    private String retValue;

    private transient int id;
    private transient int threadId;
    @JSONField(serialize=false)
    private transient Pair<Integer, Integer> pairID;
    @JSONField(serialize=false)
    private transient OPERATION_TYPE operationType = OPERATION_TYPE.UPDATE;

    public Invocation() {
        ;
    }

    public void setRetValue(String retValue) {
        this.retValue = retValue;
    }

    public String getRetValue() {
        return retValue;
    }

    public void setMethodName(String name) {
        methodName = name;
    }

    public void setArguments(List<Object> arguments) {
        this.arguments = arguments;
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
        return methodName + ":" + arguments.toString() + ":" + retValue;
    }

//    public Invocation clone() {
//        Invocation invocation = new Invocation();
//        invocation.setMethodName(this.getMethodName());
//        invocation.setRetValue(this.getRetValue());
//        invocation.setOperationType(this.getOperationType());
//        invocation.id = id;
//    }
}


