package datatype;

import history.HBGNode;
import history.Invocation;
import history.loader.PlainOperation;
import datatype.OperationTypes.OPERATION_TYPE;

import java.util.*;

public class RiakMap extends AbstractDataType {
    private HashMap<Integer, Integer> data = new HashMap<>();

    @Override
    public boolean step(Invocation invocation) {
        switch (invocation.getMethodName()) {
            case "put": {
                Integer key = (Integer) invocation.getArguments().get(0);
                Integer value = (Integer) invocation.getArguments().get(1);
                data.put(key, value);
                return true;
            }
            case "get": {
                Integer key = (Integer) invocation.getArguments().get(0);
                Integer value = data.get(key);
                if (invocation.getRetValues().size() == 0) {
                    return value == null;
                } else {
                    return value == invocation.getRetValues().get(0);
                }
            }
            case "containsValue": {
                Integer value = (Integer) invocation.getArguments().get(0);
                Boolean flag = data.containsValue(value);
                if (invocation.getRetValues().size() == 0 || (Integer) invocation.getRetValues().get(0) == 0) {
                    return !flag;
                } else {
                    return flag;
                }
            }
            case "size": {
                Integer sz = (Integer)data.size();
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

//    @Override
//    public String excute(Invocation invocation) throws Exception {
//        String methodName = invocation.getMethodName();
//        if (methodName.equals("put")) {
//            return put(invocation);
//        } else if (methodName.equals("get")) {
//            return get(invocation);
//        } else if (methodName.equals("containsValue")) {
//            return containsValue(invocation);
//        } else if (methodName.equals("size")) {
//            return size(invocation);
//        } else {
//            throw new Exception("Wrong operation: " + methodName);
//        }
//    }

    @Override
    public boolean isReadCluster(Invocation invocation) {
        if (invocation.getMethodName().equals("get")) {
            return true;
        }
        return false;
    }

    protected boolean isRelated(Invocation src, Invocation dest) {
        if (src.isQuery()) {
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

//    public Invocation generateInvocation(PlainOperation record) {
//        Invocation invocation = new Invocation();
//        invocation.setRetValue(record.getRetValue());
//        invocation.setMethodName(record.getOperationName());
//        invocation.setOperationType(getOperationType(record.getOperationName()));
//
//        if (record.getOperationName().equals("put")) {
//            invocation.addArguments(Long.parseLong(record.getArgument(0)));
//            invocation.addArguments(Long.parseLong(record.getArgument(1)));
//        } else if (record.getOperationName().equals("get")) {
//            invocation.addArguments(Long.parseLong(record.getArgument(0)));
//        } else if (record.getOperationName().equals("containsValue")) {
//            invocation.addArguments(Long.parseLong(record.getArgument(0)));
//        } else if (record.getOperationName().equals("size")) {
//            ;
//        } else {
//            System.out.println("Unknown operation");
//        }
//        return invocation;
//    }

    @Override
    public boolean isDummyOperation(HBGNode node) {
        Invocation invocation = node.getInvocation();
        if (invocation.getMethodName().equals("size") && (invocation.getRetValues().size() == 0 || (Integer) invocation.getRetValues().get(0) == 0)) {
            return true;
        }
        if (invocation.getMethodName().equals("get") && invocation.getRetValues().size() == 0) {
            return true;
        }
        return false;
    }

    public void reset() {
        data = new HashMap<>();
    }
}
