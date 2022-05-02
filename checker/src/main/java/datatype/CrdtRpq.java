package datatype;

import history.HBGNode;
import history.Invocation;

import java.util.*;

public class CrdtRpq extends AbstractDataType {
    private ArrayList<RpqElement> data = new ArrayList<>();
    private HashMap<Integer, RpqElement> map = new HashMap<>();

    @Override
    public boolean step(Invocation invocation) {
        switch (invocation.getMethodName()) {
            case "add": {
                Integer key = (Integer) invocation.getArguments().get(0);
                Integer value = (Integer) invocation.getArguments().get(1);
                add(key, value);
                return true;
            }
            case "incrby": {
                Integer key = (Integer) invocation.getArguments().get(0);
                Long delta = (Long) invocation.getArguments().get(1);
                inc(key, delta.intValue());
                return true;
            }
            case "rem": {
                Integer key = (Integer) invocation.getArguments().get(0);
                rem(key);
                return true;
            }
            case "score": {
                Integer key = (Integer) invocation.getArguments().get(0);
                Integer value = getValue(key);
                if (invocation.getRetValues().size() == 0) {
                    return value == null;
                } else {
                    return value == invocation.getRetValues().get(0);
                }
            }
            case "max": {
                List<Integer> result = getMaximumElement();
                if (result.size() == 0) {
                    return invocation.getRetValues().size() == 0;
                } else {
                    return invocation.getRetValues().size() > 0 && invocation.getRetValues().get(0) == result.get(0) && invocation.getRetValues().get(1) == result.get(1);
                }
            }
            default:
                System.out.println("Wrong Operation");
        }
        return false;
    }

//    @Override
//    public String excute(Invocation invocation) throws Exception {
//        String methodName = invocation.getMethodName();
//        if (methodName.equals("add")) {
//            return add(invocation);
//        } else if (methodName.equals("rem")) {
//            return rem(invocation);
//        } else if (methodName.equals("incrby")) {
//            return incrby(invocation);
//        } else if (methodName.equals("score")) {
//            return score(invocation);
//        } else if (methodName.equals("max")) {
//            return max(invocation);
//        } else {
//            throw new Exception("Wrong operation: " + methodName);
//        }
//    }

    protected boolean isRelated(Invocation src, Invocation dest) {
        if (src.isQuery()) {
            if (src.getId() == dest.getId()) {
                return true;
            }
            if (src.getMethodName().equals("score")) {
                Integer ele = (Integer) src.getArguments().get(0);
                if (dest.isUpdate() && dest.getArguments().get(0).equals(ele)) {
                    return true;
                } 
            } else if (src.getMethodName().equals("max")) {
                if (src.getRetValues().size() > 0) {
                    Integer ele = (Integer) src.getRetValues().get(0);
                    if (dest.isUpdate() && dest.getArguments().get(0).equals(ele)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    @Override
    public boolean isReadCluster(Invocation invocation) {
        if (invocation.getMethodName().equals("score")) {
            return true;
        }
        if (invocation.getMethodName().equals("max") && invocation.getRetValues().size() > 0) {
            return true;
        }
        return false;
    }

    @Override
    public boolean isDummyOperation(HBGNode node) {
        Invocation invocation = node.getInvocation();
        if (invocation.getMethodName().equals("max") && invocation.getRetValues().size() == 0) {
            return true;
        }
        if (invocation.getMethodName().equals("score") && invocation.getRetValues().size() == 0) {
            return true;
        }
        return false;
    }

    @Override
    public void reset() {
        map = new HashMap<>();
        data = new ArrayList<>();
    }

    private void shiftUp(int s) {
        int j = s, i = (j - 1) / 2;
        RpqElement temp = data.get(j);
        while (j > 0)
        {
            if (data.get(i).compare(temp) >= 0)
                break;
            else
                {
                    data.set(j, data.get(i));
                    data.get(j).setIndex(j);
                    j = i;
                    i = (i - 1) / 2;
                }
        }
        temp.setIndex(j);
        data.set(j, temp);
    }

    private void shiftDown(Integer s)
    {
        int i = s, j = 2 * i + 1, tail = data.size() - 1;
        RpqElement temp;
        if (i < data.size()) {
            temp = data.get(i);
        } else {
            return;
        }
        while (j <= tail)
        {
            if (j < tail && data.get(j).compare(data.get(j + 1)) < 0)
                j++;
            if (temp.compare(data.get(j)) >= 0)
                break;
            else {
                data.set(i, data.get(j));
                data.get(i).setIndex(i);
                i = j;
                j = i * 2 + 1;
            }
        }
        temp.setIndex(i);
        data.set(i, temp);
    }

    private void add(Integer k, Integer v)
    {
        if (!map.containsKey(k)) {
            RpqElement element = new RpqElement(k, v);
            map.put(k, element);
            data.add(element);
            shiftUp(data.size() - 1);
        } else {
            RpqElement element = map.get(k);
            if (element.getTemp()) {
                inc(k, v);
                element.setTemp(false);
            }
        }
    }

    private void rem(Integer k)
    {
        if (map.containsKey(k))
        {
            Integer i = map.get(k).getIndex();
            map.remove(k);
            data.set(i.intValue(), data.get(data.size() - 1));
            data.remove(data.size() - 1);
            shiftDown(i);
        }
    }

    private void inc(Integer k, Integer i)
    {
        if (i == 0)
            return;
        if (map.containsKey(k))
        {
            map.get(k).inc(i);
            if (i > 0)
                shiftUp(map.get(k).getIndex());
            else
                shiftDown(map.get(k).getIndex());
        } else {
            RpqElement element = new RpqElement(k, i);
            element.setTemp(true);
            map.put(k, element);
            data.add(element);
            shiftUp(data.size() - 1);
        }
    }

    private List<Integer> getMaximumElement() {
        List<Integer> result = new ArrayList<>(2);
        if (data.size() == 0) {
            return result;
        } else {
            RpqElement max = data.get(0);
            Integer val = max.getVal();
            Integer ele = max.getEle();
            result.add(ele);
            result.add(val);
            return result;
        }
    }

    private String max()
    {
        if (data.size() == 0) {
            //return "rwfzscore:" + "NONE";
            return "null";
        } else {
            RpqElement max = data.get(0);
            Integer val = max.getVal();
            Integer ele = max.getEle();
            //return "rwfzmax:" + Integer.toString(max.getEle()) + ":" + val.stripTrailingZeros().toPlainString();
            //return ele.toString() + " " + val.toString();
            return ele.toString() + " " + val.toString();
        }
    }

    private Integer getValue(Integer k) {
        if (data.size() == 0 || !map.containsKey(k)) {
            return null;
        } else {
            Integer val = map.get(k).getVal();
            return val;
        }
    }

    private String score(Integer k) {
        if (data.size() == 0 || !map.containsKey(k)) {
            //return "rwfzscore:" + Integer.toString(k) + ":" + "NONE";
            return "null";
        } else {
            Integer val = map.get(k).getVal();
            //return "rwfzscore:" + Integer.toString(k) + ":" + val.stripTrailingZeros().toPlainString();
            return val.toString();
        }
    }

    public String add(Invocation invocation) {
        Integer k = (Integer) invocation.getArguments().get(0);
        Integer i = (Integer) invocation.getArguments().get(1);
        add(k, i);
        return "null";
        //return invocation.getMethodName() + ":" + Integer.toString(k) + ":" + v.toString();
    }

    public String rem(Invocation invocation) {
        Integer k = (Integer) invocation.getArguments().get(0);
        rem(k);
        return "null";
        //return invocation.getMethodName() + ":" + Integer.toString(k);
    }

    public String incrby(Invocation invocation) {
        Integer k = (Integer) invocation.getArguments().get(0);
        Integer i = (Integer) invocation.getArguments().get(1);
        inc(k, i);
        return "null";
        //return invocation.getMethodName() + ":" + Integer.toString(k) + ":" + i.toString();
    }

    public String score(Invocation invocation) {
        Integer k = (Integer) invocation.getArguments().get(0);
        return score(k);
        //return invocation.getMethodName();
    }

    public String max(Invocation invocation) {
        return max();
    }
}

class RpqElement {
    private Integer ele;
    private Integer val;
    private Integer index;
    private Boolean temp = false;

    public RpqElement(Integer ele, Integer val) {
        this.ele = ele;
        this.val = val;
    }

    public Integer getVal() {
        return val;
    }

    public Integer getEle() {
        return ele;
    }

    public void setEle(Integer ele) {
        this.ele = ele;
    }

    public void setVal(Integer val) {
        this.val = val;
    }

    public Integer getIndex() {
        return index;
    }

    public void setIndex(Integer index) {
        this.index = index;
    }

    public Boolean getTemp() {
        return temp;
    }

    public void setTemp(Boolean temp) {
        this.temp = temp;
    }

    public void inc(Integer i) {
        val = val + i;
    }

    public int compare(RpqElement element) {
        if (this.val < element.val) {
            return -1;
        } else if (this.val > element.val) {
            return 1;
        } else {
            if (this.ele == element.ele) {
                return 0;
            } else if (this.ele > element.ele) {
                return 1;
            } else {
                return -1;
            }
        }
    }

    @Override
    public int hashCode() {
        return Objects.hash(ele, val, index);
    }
}
