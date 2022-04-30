package datatype;

import history.HBGNode;
import history.Invocation;
import traceprocessing.Record;
import datatype.OperationTypes.OPERATION_TYPE;

import java.util.*;

public class RiakMap extends AbstractDataType {
    private HashMap<Integer, Integer> data = new HashMap<>();

    @Override
    public String excute(Invocation invocation) throws Exception {
        String methodName = invocation.getMethodName();
        if (methodName.equals("put")) {
            return put(invocation);
        } else if (methodName.equals("get")) {
            return get(invocation);
        } else if (methodName.equals("containsValue")) {
            return containsValue(invocation);
        } else if (methodName.equals("size")) {
            return size(invocation);
        } else {
            throw new Exception("Wrong operation: " + methodName);
        }
    }

    public OPERATION_TYPE getOperationType(String methodName) {
        if (operationTypes == null) {
            operationTypes = new OperationTypes();
            operationTypes.setOperationType("put", OPERATION_TYPE.UPDATE);
            operationTypes.setOperationType("get", OPERATION_TYPE.QUERY);
            operationTypes.setOperationType("containsValue", OPERATION_TYPE.QUERY);
            operationTypes.setOperationType("size", OPERATION_TYPE.QUERY);
            return operationTypes.getOperationType(methodName);
        } else {
            return operationTypes.getOperationType(methodName);
        }
    }

    @Override
    public boolean isReadCluster(Invocation invocation) {
        if (invocation.getMethodName().equals("get")) {
            return true;
        }
        return false;
    }

    protected boolean isRelated(Invocation src, Invocation dest) {
        if (src.getOperationType() == OPERATION_TYPE.QUERY) {
            if (src.getMethodName().equals("get")) {
                if (src.getId() == dest.getId()) {
                    return true;
                }
                Integer key = (Integer) src.getArguments().get(0);
                if (dest.getMethodName().equals("put") && dest.getArguments().get(0).equals(key)) {
                    return true;
                } 
            }
        }
        return false;
    }

    public Invocation generateInvocation(Record record) {
        Invocation invocation = new Invocation();
        invocation.setRetValue(record.getRetValue());
        invocation.setMethodName(record.getOperationName());
        invocation.setOperationType(getOperationType(record.getOperationName()));

        if (record.getOperationName().equals("put")) {
            invocation.addArguments(Integer.parseInt(record.getArgument(0)));
            invocation.addArguments(Integer.parseInt(record.getArgument(1)));
        } else if (record.getOperationName().equals("get")) {
            invocation.addArguments(Integer.parseInt(record.getArgument(0)));
        } else if (record.getOperationName().equals("containsValue")) {
            invocation.addArguments(Integer.parseInt(record.getArgument(0)));
        } else if (record.getOperationName().equals("size")) {
            ;
        } else {
            System.out.println("Unknown operation");
        }
        return invocation;
    }

    @Override
    public boolean isDummyOperation(HBGNode node) {
        Invocation invocation = node.getInvocation();
        if (invocation.getMethodName().equals("size") && invocation.getRetValue().equals("0")) {
            return true;
        }
        if (invocation.getMethodName().equals("get") && invocation.getRetValue().equals("null")) {
            return true;
        }
        return false;
    }

    public void reset() {
        data = new HashMap<>();
    }

    public AbstractDataType createInstance() {
        return new RiakMap();
    }

    public String put(Invocation invocation) {
        Integer key = (Integer) invocation.getArguments().get(0);
        Integer value = (Integer) invocation.getArguments().get(1);
        data.put(key, value);
        return "null";
    }

    public String get(Invocation invocation) {
        Integer key = (Integer) invocation.getArguments().get(0);
        Integer value = data.get(key);
        if (value != null) {
            return Integer.toString(value);
        } else {
            return "null";
        }
    }

    public String containsValue(Invocation invocation) {
        Integer value = (Integer) invocation.getArguments().get(0);
        if (data.containsValue(value)) {
            return "true";
        } else {
            return "false";
        }
    }

    public String size(Invocation invocation) {
        return Integer.toString(data.size());
    }
}
