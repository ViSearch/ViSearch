package traceprocessing;

import datatype.AbstractDataType;
import history.Invocation;

import java.util.ArrayList;
import java.util.List;

public class Record implements Comparable<Record> {
    private long startTime;
    private long endTime;
    private String operationName;
    private List<String> arguments;
    private String retValue;

    public Record(String operationName, List<String> arguments, String retValue) {
        this.operationName = operationName;
        this.arguments = arguments;
        this.retValue = retValue;
    }
    public Record(String line) {
        String[] cols = line.split(",");
        this.startTime = Long.parseLong(cols[0]);
        this.endTime = Long.parseLong(cols[1]);
        this.operationName = cols[2];
        this.retValue = cols[cols.length - 1];
        arguments = new ArrayList<>();
        for (int i = 3; i < cols.length - 1; i++) {
            arguments.add(cols[i]);
        }
    }

    public Invocation generateInvocation(AbstractDataType adt) {
        if (adt == null) {
            System.out.println("ADT is null");
            return null;
        }
        return adt.generateInvocation(this);
    }

    public String getOperationName() {
        return operationName;
    }

    public String getRetValue() {
        return retValue;
    }

    public String getArgument(int index) {
        return arguments.get(index);
    }

    @Override
    public String toString() {
        String str = operationName + ":";
        for (String arg : arguments) {
            str += " " + arg;
        }
        str += "=" + retValue;
        return str;
    }

    @Override
    public int compareTo(Record o) {
        if (o.startTime > this.endTime) {
            return -1;
        } else if (o.endTime < this.startTime) {
            return 1;
        } else {
            return 0;
        }
    }
}
