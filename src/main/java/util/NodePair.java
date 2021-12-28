package util;

import history.HBGNode;
import org.apache.commons.lang3.tuple.ImmutablePair;

public class NodePair {
    public HBGNode left;
    public HBGNode right;

    public NodePair(HBGNode left, HBGNode right) {
        this.left = left;
        this.right = right;
    }

    public NodePair(NodePair pair) {
        this.left = pair.left;
        this.right = pair.right;
    }

    public NodePair(ImmutablePair<HBGNode, HBGNode> pair) {
        this.left = pair.left;
        this.right = pair.right;
    }

    public HBGNode getLeft() {
        return left;
    }

    public HBGNode getRight() {
        return right;
    }

    public NodePair getReversal() {
        return new NodePair(right, left);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (this == obj) {
            return true;
        }
        NodePair pair = (NodePair) obj;
        return pair.left.equals(this.left) && pair.right.equals(this.right);
    }

    @Override
    public int hashCode() {
        return (53 + left.hashCode()) * 53 + right.hashCode();
    }

    @Override
    public String toString() {
        return "NodePair{" +
                left +
                " => " + right +
                '}';
    }
}
