package datatype;

import history.HBGNode;
import history.HappenBeforeGraph;
import history.Invocation;
import history.loader.PlainOperation;
import datatype.OperationTypes.OPERATION_TYPE;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractDataType {
    protected OperationTypes operationTypes = null;

    public abstract String excute(Invocation invocation) throws Exception;

    public abstract boolean step(Invocation invocation);

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

    public boolean isDummyOperation(HBGNode node) {
        return false;
    }

    protected boolean isRelated(Invocation src, Invocation dest) {
        return false;
    }

    public abstract void reset();

    public abstract Invocation generateInvocation(PlainOperation record);

    public abstract OPERATION_TYPE getOperationType(String methodName);
}
