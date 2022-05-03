package datatype;

import history.HBGNode;
import history.HappenBeforeGraph;
import history.Invocation;

import java.util.ArrayList;
import java.util.List;

public interface AbstractDataType {
    public boolean step(Invocation invocation);

    public default List<List<HBGNode>> getRelatedOperations(HBGNode node, HappenBeforeGraph happenBeforeGraph) {
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

    public default boolean isReadCluster(Invocation invocation) {
        return false;
    }

    public default boolean isDummyOperation(HBGNode node) {
        return false;
    }

    public default boolean isRelated(Invocation src, Invocation dest) {
        return false;
    }

    public void reset();
}
