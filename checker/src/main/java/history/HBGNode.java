package history;

import java.io.Serializable;
import java.util.*;

public class HBGNode implements Serializable {
    private Invocation invocation;
    private HBGNode prev = null;
    private HBGNode next = null;

    public HBGNode() {
        ;
    }

    public HBGNode(Invocation invocation, int id) {
        this.invocation = invocation;
        invocation.setId(id);
    }

    public int getId() {
        return invocation.getId();
    }

    public int getThreadId() {
        return invocation.getThreadId();
    }

    public void setThreadId(int threadId) {
        invocation.setThreadId(threadId);
    }

    public Invocation getInvocation() {
        return invocation;
    }

    public void setNext(HBGNode next) {
        this.next = next;
    }

    public void setPrev(HBGNode prev) {
        this.prev = prev;
    }

    public HBGNode getNext() {
        return next;
    }

    public HBGNode getPrev() {
        return prev;
    }

    public List<HBGNode> getAllPrevs() {
        List<HBGNode> result = new LinkedList<>();
        HBGNode node = this;
        while (node.getPrev() != null) {
            node = node.getPrev();
            result.add(node);
        }
        return result;
    }

    @Override
    public String toString() { ;
        return getInvocation().getPairID().toString() + " " + invocation.toString();
    }

    @Override
    public int hashCode() {
        return getId();
    }

    @Override
    public HBGNode clone() {
        return new HBGNode(this.invocation, this.getId());
    }

    public boolean equals(Object node) {
        if (node == null) {
            return false;
        }
        if (this == node) {
            return true;
        }
        return this.hashCode() == node.hashCode();
    }
}
