package history;

import util.NodePair;

import java.io.Serializable;
import java.util.*;

public class LinVisibility implements Serializable, Iterable<HBGNode> {
    private HashMap<HBGNode, Set<HBGNode>> visibility = new HashMap<>();

    public void setVisibility(HashMap<HBGNode, Set<HBGNode>> visibility) {
        this.visibility = visibility;
    }

    public void cleanVisibility() {
        visibility = new HashMap<>();
    }

    public Set<HBGNode> getNodeVisibility(HBGNode node) {
        return visibility.get(node);
    }

    public void updateNodeVisibility(HBGNode node, Set<HBGNode> vis) {
        visibility.put(node, vis);
    }

    public void removeNodeVisibility(HBGNode node) {
        visibility.remove(node);
    }

    public String toString() {
        String result = "";
        for (Map.Entry<HBGNode, Set<HBGNode>> entry : visibility.entrySet()) {
            if (entry.getValue().size() > 1) {
                result += entry.toString() + "\n";
            }
        }
        return result;
    }

    public int size() {
        return visibility.size();
    }

    public Iterator<HBGNode> iterator() {
        return visibility.keySet().iterator();
    }

    @Override
    public Object clone() {
        LinVisibility newVis = new LinVisibility();
        newVis.visibility = new HashMap<>(this.visibility);
        return newVis;
    }
}
