package datatype;

import history.HBGNode;
import history.HappenBeforeGraph;
import history.Invocation;
import traceprocessing.Record;
import datatype.OperationTypes.OPERATION_TYPE;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public abstract class AbstractDataType {
    protected OperationTypes operationTypes = null;

    public abstract String excute(Invocation invocation) throws Exception;

    public List<List<HBGNode>> getRelatedOperations(HBGNode node, HappenBeforeGraph happenBeforeGraph) {
        List<List<HBGNode>> lists = new ArrayList<>();
        for (HBGNode startNode : happenBeforeGraph.getStartNodes()) {
            List<HBGNode> tempList = new ArrayList<>();
            HBGNode temp = startNode;
            while (temp != null) {
                if (this.isRelated(node.getInvocation(), temp.getInvocation())) {
                    tempList.add(temp);
                }
                if (temp.equals(node)) {
                    break;
                }
                temp = temp.getNext();
            }
            if (tempList.size() > 0) {
                lists.add(tempList);
            }
        }
        return lists;
    }

    public boolean isReadCluster(Invocation invocation) {
        if (invocation.getOperationType() == OPERATION_TYPE.QUERY) {
            return true;
        } else {
            return false;
        }
    }

    public abstract boolean isDummyOperation(HBGNode node);

    protected abstract boolean isRelated(Invocation src, Invocation dest);

    public abstract void reset();

    public abstract void print();

    public abstract int hashCode();

    public abstract Invocation generateInvocation(Record record);

    public abstract AbstractDataType createInstance();

    public abstract OPERATION_TYPE getOperationType(String methodName);
}
