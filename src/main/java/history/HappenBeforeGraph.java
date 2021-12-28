package history;

import com.google.common.collect.HashMultimap;
import org.apache.commons.lang3.tuple.ImmutablePair;

import java.util.*;

public class HappenBeforeGraph implements Iterable<HBGNode>, Cloneable {
    private List<HBGNode> startNodes = new ArrayList<>();
    private HashMap<Integer, HBGNode> nodes = new HashMap<>();

    public HappenBeforeGraph() {
        ;
    }

    public HappenBeforeGraph(Program program) {
        List<List<Invocation>> subPrograms = program.getSubPrograms();
        int index = 0;
        for (int k = 0; k < subPrograms.size(); k++) {
            List<Invocation> sp = subPrograms.get(k);
            for (int i = 0; i < sp.size(); i++) {
                HBGNode node = new HBGNode(sp.get(i), index);
                node.getInvocation().setPairID(new ImmutablePair<>(k, i));
                node.setThreadId(k);
                nodes.put(index, node);
                if (i == 0) {
                    startNodes.add(node);
                } else {
                    node.setPrev(nodes.get(index-1));
                    nodes.get(index-1).setNext(node);
                }
                index++;
            }
        }
    }

    public HappenBeforeGraph(List<List<HBGNode>> nodes) {
        for (List<HBGNode> list : nodes) {
            HBGNode lastNode = null;
            for (int i = 0; i < list.size(); i++) {
                HBGNode node = list.get(i).clone();
                this.nodes.put(node.getId(), node);
                if (i == 0) {
                    startNodes.add(node);
                } else {
                    node.setPrev(lastNode);
                    lastNode.setNext(node);
                }
                lastNode = node;
            }
        }
    }

    public Iterator<HBGNode> iterator() {
        return nodes.values().iterator();
    }

    public int size() {
        return nodes.size();
    }

    public int threadNum() {
        return startNodes.size();
    }

    public HBGNode get(int id) {
        return nodes.get(id);
    }

    public List<HBGNode> getStartNodes() {
        return startNodes;
    }

    public void removeNode(HBGNode node) {
        nodes.remove(node.getId());
    }

    @Override
    public String toString() {
        return "HappenBeforeGraph{" +
                "startNodes=" + startNodes +
                ", nodes=" + nodes +
                '}';
    }
}


