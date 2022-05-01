package datatype;

import history.HBGNode;
import history.HappenBeforeGraph;
import history.Invocation;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractDataType {
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
        return false;
    }

    public boolean isDummyOperation(HBGNode node) {
        return false;
    }

    protected boolean isRelated(Invocation src, Invocation dest) {
        return false;
    }

    public abstract void reset();
}
