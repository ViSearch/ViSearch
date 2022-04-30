package datatype;

import history.HBGNode;
import history.Invocation;
import traceprocessing.Record;
import datatype.OperationTypes.OPERATION_TYPE;

import java.util.*;

public class RiakMap extends AbstractDataType {
    private HashMap<Long, Long> data = new HashMap<>();

    @Override
    public boolean step(Invocation invocation) {
        switch (invocation.getMethodName()) {
            case "put": {
                Long key = (Long) invocation.getArguments().get(0);
                Long value = (Long) invocation.getArguments().get(1);
                data.put(key, value);
                return true;
            }
            case "get": {
                Long key = (Long) invocation.getArguments().get(0);
                Long value = data.get(key);
                if (invocation.getRetValues().size() == 0) {
                    return value == null;
                } else {
                    return value == invocation.getRetValues().get(0);
                }
            }
            case "containsValue": {
                Long value = (Long) invocation.getArguments().get(0);
                Boolean flag = data.containsValue(value);
                if (invocation.getRetValues().size() == 0 || (Long) invocation.getRetValues().get(0) == 0) {
                    return !flag;
                } else {
                    return flag;
                }
            }
            case "size": {
                Long sz = (long)data.size();
                if (invocation.getRetValues().size() == 0) {
                    return sz == 0;
                } else {
                    return sz == invocation.getRetValues().get(0);
                }
            }
            default:
                System.out.println("Wrong Operation");
        }
        return false;
    }

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
                Long key = (Long) src.getArguments().get(0);
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
            invocation.addArguments(Long.parseLong(record.getArgument(0)));
            invocation.addArguments(Long.parseLong(record.getArgument(1)));
        } else if (record.getOperationName().equals("get")) {
            invocation.addArguments(Long.parseLong(record.getArgument(0)));
        } else if (record.getOperationName().equals("containsValue")) {
            invocation.addArguments(Long.parseLong(record.getArgument(0)));
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
        Long key = (Long) invocation.getArguments().get(0);
        Long value = (Long) invocation.getArguments().get(1);
        data.put(key, value);
        return "null";
    }

    public String get(Invocation invocation) {
        Long key = (Long) invocation.getArguments().get(0);
        Long value = data.get(key);
        if (value != null) {
            return Long.toString(value);
        } else {
            return "null";
        }
    }

    public String containsValue(Invocation invocation) {
        Long value = (Long) invocation.getArguments().get(0);
        if (data.containsValue(value)) {
            return "true";
        } else {
            return "false";
        }
    }

    public String size(Invocation invocation) {
        return Long.toString(data.size());
    }
}
