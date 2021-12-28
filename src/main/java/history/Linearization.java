package history;

import datatype.OperationTypes;
import rule.RuleTable;
import util.NodePair;

import java.io.Serializable;
import java.util.*;

public class Linearization implements Serializable, Iterable<HBGNode> {
    private List<HBGNode> lin = new ArrayList<>();
    private List<HBGNode> front = new ArrayList<>();

    public Linearization() {
        ;
    }

    public Set<NodePair> extractHBRelation() {
        Set<NodePair> hbs = new HashSet<>();
        for (int i = 1; i < lin.size(); i++) {
            for (int j = 0; j < i; j++) {
                if (lin.get(j).getThreadId() != lin.get(i).getThreadId()) {
                    hbs.add(new NodePair(lin.get(j), lin.get(i)));
                }
            }
        }
        return hbs;
    }

    public void add(HBGNode node) {
        lin.add(node);
    }

    public String getRetValueTrace(int index) {
        ArrayList<String> retTrace = new ArrayList<>();
        for (int i = 0; i < index; i++) {
            retTrace.add(lin.get(i).getInvocation().getRetValue());
        }
        return  retTrace.toString();
    }

    public boolean contains(HBGNode node) {
        return contains(node.getId());
    }

    public boolean contains(Integer id) {
        for (HBGNode node : lin) {
            if (node.getId() == id) {
                return true;
            }
        }
        return false;
    }

    public int indexOf(HBGNode node) {
        return lin.indexOf(node);
    }

    public void addFront(List<HBGNode> nodes) {
        front.addAll(nodes);
    }

    public HBGNode getLast() {
        return lin.get(lin.size() - 1);
    }

    public HBGNode get(int index) {
        return lin.get(index);
    }

    public int size() {
        return lin.size();
    }

    public int getQueryOperationSize() {
        int sz = 0;
        for (HBGNode node : lin) {
            if (node.getInvocation().getOperationType() == OperationTypes.OPERATION_TYPE.QUERY) {
                sz++;
            }
        }
        return sz;
    }

    public Iterator<HBGNode> iterator() {
        return lin.iterator();
    }

    public Linearization prefix(int index) {
        if (index < 0 || index >= lin.size()) {
            return null;
        } else {
            Linearization sub = new Linearization();
            for (int i = 0; i <= index; i++) {
                sub.add(lin.get(i));
            }
            return sub;
        }
    }

    public Linearization prefix(HBGNode node) {
        for (int i = 0; i < lin.size(); i++) {
            if (node.equals(lin.get(i))) {
                return prefix(i);
            }
        }
        return null;
    }

    public List<Linearization> extend(RuleTable ruleTable) {
        List<Linearization> extentLins = new ArrayList<>();
        for (int i = 0; i < front.size(); i++) {
            HBGNode node = front.get(i);
            if (node == null) {
                continue;
            }
            if (ruleTable == null || ruleTable.linearizationFilter(this, node)) {
                Linearization linearization = (Linearization) this.clone();
                linearization.add(node);
                linearization.front.set(i, node.getNext());
                extentLins.add(linearization);
            }
        }
        return extentLins;
    }

    public List<Linearization> extend() {
        List<Linearization> extentLins = new ArrayList<>();
        for (int i = 0; i < front.size(); i++) {
            HBGNode node = front.get(i);
            if (node == null) {
                continue;
            }
            Linearization linearization = (Linearization) this.clone();
            linearization.add(node);
            linearization.front.set(i, node.getNext());
            extentLins.add(linearization);
        }
        return extentLins;
    }

    public String toString() {
        ArrayList<String> list = new ArrayList<>();
        for (HBGNode node : lin) {
            list.add(node.toString());
        }
        return list.toString();
    }

    @Override
    public Object clone() {
        Linearization newLin = new Linearization();
        newLin.lin = new ArrayList<>(this.lin);
        newLin.front = new ArrayList<>(this.front);
        return newLin;
    }
}
