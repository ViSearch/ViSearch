package datatype;

import history.HBGNode;
import history.Invocation;
import traceprocessing.Record;
import datatype.OperationTypes.OPERATION_TYPE;

import java.util.HashSet;

public class RiakSet extends AbstractDataType {
    private HashSet<Integer> data = new HashSet<>();

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
            Integer ele = (Integer) src.getArguments().get(0);
            if (dest.getOperationType() == OPERATION_TYPE.UPDATE && dest.getArguments().get(0).equals(ele)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean isDummyOperation(HBGNode node) {
        Invocation invocation = node.getInvocation();
        if (invocation.getMethodName().equals("size") && invocation.getRetValue().equals("0")) {
            return true;
        }
        if (invocation.getMethodName().equals("contains") && invocation.getRetValue().equals("false")) {
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
            invocation.addArguments(Integer.parseInt(record.getArgument(0)));
        } else if (record.getOperationName().equals("remove")) {
            invocation.addArguments(Integer.parseInt(record.getArgument(0)));
        } else if (record.getOperationName().equals("contains")) {
            invocation.addArguments(Integer.parseInt(record.getArgument(0)));
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
        Integer key = (Integer) invocation.getArguments().get(0);
        data.add(key);
        return "null";
    }

    public String remove(Invocation invocation) {
        Integer key = (Integer) invocation.getArguments().get(0);
        data.remove(key);
        return "null";
    }

    public String contains(Invocation invocation) {
        Integer key = (Integer) invocation.getArguments().get(0);
        if (data.contains(key)) {
            return "true";
        } else {
            return "false";
        }
    }

    public String size(Invocation invocation) {
        return Integer.toString(data.size());
    }
}
