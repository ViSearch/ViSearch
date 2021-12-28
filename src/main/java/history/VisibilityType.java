package history;

public enum VisibilityType {
    COMPLETE, CAUSAL, PEER, MONOTONIC, BASIC, WEAK;
    
    static public VisibilityType getVisibility(String vis) {
        if (vis.equals("COMPLETE") || vis.equals("Complete") || vis.equals("complete")) {
            return VisibilityType.COMPLETE;
        } else if (vis.equals("CAUSAL") || vis.equals("Causal") || vis.equals("causal")) {
            return VisibilityType.CAUSAL;
        } else if (vis.equals("PEER") || vis.equals("Peer") || vis.equals("peer")) {
            return VisibilityType.PEER;
        } else if (vis.equals("MONOTONIC") || vis.equals("Monotonic") || vis.equals("monotonic")) {
            return VisibilityType.MONOTONIC;
        } else if (vis.equals("BASIC") || vis.equals("Basic") || vis.equals("basic")) {
            return VisibilityType.BASIC;
        } else if (vis.equals("WEAK") || vis.equals("Weak") || vis.equals("weak")) {
            return VisibilityType.WEAK;
        } else {
            return VisibilityType.COMPLETE;
        }
    }
}
