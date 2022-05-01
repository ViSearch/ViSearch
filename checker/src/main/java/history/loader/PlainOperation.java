package history.loader;

import datatype.AbstractDataType;
import history.Invocation;

import java.util.ArrayList;
import java.util.List;

public class PlainOperation implements Comparable<PlainOperation> {
    public long startTime;
    public long endTime;
    public String operationName;
    public List<String> arguments;
    public List<String> retValues;

    public PlainOperation() {
        arguments = new ArrayList<>();
        retValues = new ArrayList<>();
    }

    public PlainOperation(String operationName, List<String> arguments, List<String> retValues, long startTime, long endTime) {
        this.operationName = operationName;
        this.arguments = arguments;
        this.retValues = retValues;
        this.startTime = startTime;
        this.endTime = endTime;
    }
//    public PlainOperation(String line) {
//        String[] cols = line.split(",");
//        this.startTime = Long.parseLong(cols[0]);
//        this.endTime = Long.parseLong(cols[1]);
//        this.operationName = cols[2];
//        this.retValue = cols[cols.length - 1];
//        arguments = new ArrayList<>();
//        for (int i = 3; i < cols.length - 1; i++) {
//            arguments.add(cols[i]);
//        }
//    }

    public Invocation generateInvocation(AbstractDataType adt) {
        if (adt == null) {
            System.out.println("ADT is null");
            return null;
        }
        return adt.generateInvocation(this);
    }

    @Override
    public String toString() {
        String str = operationName + ":";
        for (String arg : arguments) {
            str += " " + arg;
        }
        str += "=" + retValues;
        return str;
    }

    @Override
    public int compareTo(PlainOperation o) {
        if (o.startTime > this.endTime) {
            return -1;
        } else if (o.endTime < this.startTime) {
            return 1;
        } else {
            return 0;
        }
    }
}
