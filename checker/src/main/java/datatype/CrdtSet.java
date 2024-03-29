package datatype;

import history.HBGNode;
import history.Invocation;

import java.util.HashSet;

public class CrdtSet implements AbstractDataType {
    private HashSet<Integer> data = new HashSet<>();

    @Override
    public boolean step(Invocation invocation) {
        switch (invocation.getMethodName()) {
            case "add": {
                Integer key = (Integer) invocation.getArguments().get(0);
                data.add(key);
                return true;
            }
            case "remove": {
                Integer key = (Integer) invocation.getArguments().get(0);
                data.remove(key);
                return true;
            }
            case "contains": {
                Integer key = (Integer) invocation.getArguments().get(0);
                boolean flag = data.contains(key);
                if (invocation.getRetValues().size() == 0 || (Long) invocation.getRetValues().get(0) == 0) {
                    return !flag;
                } else {
                    return flag;
                }
            }
            case "size": {
                int sz = data.size();
                if (invocation.getRetValues().size() == 0) {
                    return sz == 0;
                } else {
                    return sz == ((Long)invocation.getRetValues().get(0)).intValue();
                }
            }
            default:
                System.out.println("Wrong Operation");
        }
        return false;
    }

    public boolean isReadCluster(Invocation invocation) {
        if (invocation.getMethodName().equals("contains")) {
            return true;
        } else {
            return false;
        }
    }

    public boolean isRelated(Invocation src, Invocation dest) {
       if (src.isQuery()) {
            if (src.getId() == dest.getId()) {
                return true;
            }
            Integer ele = (Integer) src.getArguments().get(0);
            if (dest.isUpdate() && dest.getArguments().get(0).equals(ele)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean isDummyOperation(HBGNode node) {
        Invocation invocation = node.getInvocation();
        if (invocation.getMethodName().equals("size") && (invocation.getRetValues().size() == 0 || (Long) invocation.getRetValues().get(0) == 0)) {
            return true;
        }
        if (invocation.getMethodName().equals("contains") && (invocation.getRetValues().size() == 0 || (Long) invocation.getRetValues().get(0) == 0)) {
            return true;
        }
        return false;
    }

    public void reset() {
        data = new HashSet<>();
    }

}
