package datatype;

import history.HBGNode;
import history.Invocation;
import traceprocessing.Record;
import datatype.OperationTypes.OPERATION_TYPE;

import java.util.HashSet;
import java.util.List;

public class RiakSet extends AbstractDataType {
    private HashSet<Long> data = new HashSet<>();

    @Override
    public boolean step(Invocation invocation) {
        switch (invocation.getMethodName()) {
            case "add": {
                Long key = (Long) invocation.getArguments().get(0);
                data.add(key);
                return true;
            }
            case "rem": {
                Long key = (Long) invocation.getArguments().get(0);
                data.remove(key);
                return true;
            }
            case "contains": {
                Long key = (Long) invocation.getArguments().get(0);
                Boolean flag = data.contains(key);
                if (invocation.getRetValues().size() == 0 || (Long) invocation.getRetValues().get(0) == 0) {
                    return !flag;
                } else {
                    return flag;
                }
            }
            case "size": {
                Integer sz = data.size();
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
        if (methodName.equals("add")) {
            return add(invocation);
        } else if (methodName.equals("remove")) {
            return remove(invocation);
        } else if (methodName.equals("contains")) {
            return contains(invocation);
        } else if (methodName.equals("size")) {
            return size(invocation);
        } else {
            throw new Exception("Wrong operation: " + methodName);
        }
    }

    public OPERATION_TYPE getOperationType(String methodName) {
        if (operationTypes == null) {
            operationTypes = new OperationTypes();
            operationTypes.setOperationType("remove", OPERATION_TYPE.UPDATE);
            operationTypes.setOperationType("add", OPERATION_TYPE.UPDATE);
            operationTypes.setOperationType("contains", OPERATION_TYPE.QUERY);
            operationTypes.setOperationType("size", OPERATION_TYPE.QUERY);
            return operationTypes.getOperationType(methodName);
        } else {
            return operationTypes.getOperationType(methodName);
        }
    }

    public boolean isReadCluster(Invocation invocation) {
        if (invocation.getMethodName().equals("contains")) {
            return true;
        } else {
            return false;
        }
    }

    protected boolean isRelated(Invocation src, Invocation dest) {
       if (src.getOperationType() == OPERATION_TYPE.QUERY) {
            if (src.getId() == dest.getId()) {
                return true;
            }
            Long ele = (Long) src.getArguments().get(0);
            if (dest.getOperationType() == OPERATION_TYPE.UPDATE && dest.getArguments().get(0).equals(ele)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean isDummyOperation(HBGNode node) {
        Invocation invocation = node.getInvocation();
        if (invocation.getMethodName().equals("size") && (invocation.getRetValues().size() == 0 || (Long) invocation.getRetValues().get(0) == 0
                || (invocation.getRetValue() != null && invocation.getRetValue().equals("0")))) {
            return true;
        }
        if (invocation.getMethodName().equals("contains") && (invocation.getRetValues().size() == 0 || (Long) invocation.getRetValues().get(0) == 0
                || (invocation.getRetValue() != null && invocation.getRetValue().equals("false")))) {
            return true;
        }
        return false;
    }

    public void reset() {
        data = new HashSet<>();
    }

    public Invocation generateInvocation(Record record) {
        Invocation invocation = new Invocation();
        invocation.setRetValue(record.getRetValue());
        invocation.setMethodName(record.getOperationName());
        invocation.setOperationType(getOperationType(record.getOperationName()));

        if (record.getOperationName().equals("add")) {
            invocation.addArguments(Long.parseLong(record.getArgument(0)));
        } else if (record.getOperationName().equals("remove")) {
            invocation.addArguments(Long.parseLong(record.getArgument(0)));
        } else if (record.getOperationName().equals("contains")) {
            invocation.addArguments(Long.parseLong(record.getArgument(0)));
        } else if (record.getOperationName().equals("size")) {
           ;
        } else {
            System.out.println("Unknown operation");
        }
        return invocation;
    }

    public AbstractDataType createInstance() {
        return new RiakSet();
    }

    public String add(Invocation invocation) {
        Long key = (Long) invocation.getArguments().get(0);
        data.add(key);
        return "null";
    }

    public String remove(Invocation invocation) {
        Long key = (Long) invocation.getArguments().get(0);
        data.remove(key);
        return "null";
    }

    public String contains(Invocation invocation) {
        Long key = (Long) invocation.getArguments().get(0);
        if (data.contains(key)) {
            return "true";
        } else {
            return "false";
        }
    }

    public String size(Invocation invocation) {
        return Long.toString(data.size());
    }
}
