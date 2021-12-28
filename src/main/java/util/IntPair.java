package util;

import org.apache.commons.lang3.tuple.ImmutablePair;

public class IntPair {
    public Integer left;
    public Integer right;

    public IntPair(Integer left, Integer right) {
        this.left = left;
        this.right = right;
    }

    public IntPair(IntPair pair) {
        this.left = pair.left;
        this.right = pair.right;
    }

    public IntPair(ImmutablePair<Integer, Integer> pair) {
        this.left = pair.left;
        this.right = pair.right;
    }

    public Integer getLeft() {
        return left;
    }

    public Integer getRight() {
        return right;
    }

    public IntPair getReversal() {
        return new IntPair(right, left);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (this == obj) {
            return true;
        }
        IntPair pair = (IntPair) obj;
        return pair.left.equals(this.left) && pair.right.equals(this.right);
    }

    @Override
    public int hashCode() {
        return (53 + left) * 53 + right;
    }
}
