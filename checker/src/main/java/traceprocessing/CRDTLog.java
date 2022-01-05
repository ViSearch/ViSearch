package traceprocessing;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.io.*;
import java.util.*;

public class CRDTLog {
    List<List<CRDTOperation>> log;
    long earliestTime = Long.MAX_VALUE;
    long latestTime = Long.MIN_VALUE;

    public CRDTLog(String filepath) {
        File baseFile = new File(filepath);
        if (baseFile.isFile() || !baseFile.exists()) {
            return;
        }
        File[] files = baseFile.listFiles();
        log = new LinkedList<>();
        for (File file : files) {
            log.add(readLog(file));
        }
    }

    private List<CRDTOperation> readLog(File file) {
        try {
            BufferedReader br = new BufferedReader(new FileReader(file));
            List<CRDTOperation> log = new LinkedList<>();
            String str = null;
            while ((str = br.readLine()) != null) {
                if (!str.equals("")) {
                    CRDTOperation o = new CRDTOperation(str);
                    if (!o.isEffect) {
                        continue;
                    }
                    log.add(o);
                    if (o.timeStamp < earliestTime) {
                        earliestTime = o.timeStamp;
                    }
                    if (o.timeStamp > latestTime) {
                        latestTime = o.timeStamp;
                    }
                }
            }
            return log;
        } catch (IOException ioException) {
            ioException.printStackTrace();
            return null;
        }
    }

    public void checkChannel(int segment) {
        long interval = (latestTime - earliestTime) / segment;
        long[] checkPoints = new long[segment-1];
        for (int i = 0; i < checkPoints.length; i++) {
            checkPoints[i] = earliestTime + interval * (i+1);
        }

        Iterator<CRDTOperation>[] iters = new Iterator[log.size()];
        for (int i = 0; i < iters.length; i++) {
            iters[i] = log.get(i).iterator();
        }
        Map<CRDTOperation, Integer> map = new HashMap<>(4096);
        for (long checkPoint : checkPoints) {
            System.out.println(checkPoint(checkPoint, iters, map));
        }
//        System.out.println(checkPoint(latestTime-interval , iters, map));
//        System.out.println(checkPoint(earliestTime + interval, iters, map));
    }

    private int checkPoint(long checkPoint, Iterator<CRDTOperation>[] iters, Map<CRDTOperation, Integer> map) {
        for (Iterator<CRDTOperation> iter : iters) {
            while (iter.hasNext()) {
                CRDTOperation o = iter.next();
                Integer i = map.getOrDefault(o, 0);
                map.put(o, i+1);
                if (o.timeStamp > checkPoint) {
                    break;
                }
            }
        }
        return computeOperationRemainInChannel(iters.length, map);
    }

    private int computeOperationRemainInChannel(int replicas, Map<CRDTOperation, Integer> map) {
        int count = 0;
        Iterator<Map.Entry<CRDTOperation, Integer>> iter = map.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry<CRDTOperation, Integer> entry = iter.next();
//            System.out.println(entry.getValue());
            if (entry.getValue() % replicas == 0 && entry.getValue() > 0) {
                count++;
//                iter.remove();
            }
//            if (entry.getValue() != 9) {
//                System.out.println(entry.toString());
//            }
        }
//        System.out.println("Count: " + count);
        return map.size() - count;
    }



    public static void main(String[] args) {
        CRDTLog log = new CRDTLog("D:\\ViSearch\\crdtlog\\log8");
        log.checkChannel(300);
    }
}

class CRDTOperation {
    long timeStamp;
    boolean isEffect;
    String methodName;
    String type;
    String arguments = "";
    String vectorClock;
    public CRDTOperation(String raw) {
        int index = raw.indexOf(',');
        timeStamp = Long.parseLong(raw.substring(0, index));
        int index1 = raw.indexOf(':');
        if (raw.substring(index+2, index1).equals("EFFECT")) {
            isEffect = true;
        } else {
            isEffect = false;
            return;
        }
        String[] info = raw.substring(index1+2).split(" ");
        methodName = info[0];
        type = info[1];
        for (int i = 2; i < info.length - 1; i++) {
            arguments += info[i] + " ";
        }
//        vectorClock = info[info.length - 1].substring(0,info[info.length - 1].indexOf('|'));
        vectorClock = info[info.length - 1];
    }

    @Override
    public String toString() {
        return "CRDTOperation{" +
                "timeStamp=" + timeStamp +
                ", isEffect=" + isEffect +
                ", methodName='" + methodName + '\'' +
                ", type='" + type + '\'' +
                ", arguments='" + arguments + '\'' +
                ", vectorClock='" + vectorClock + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        CRDTOperation that = (CRDTOperation) o;

        return new EqualsBuilder()
                .append(methodName, that.methodName)
                .append(arguments, that.arguments)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return (arguments+methodName+" "+vectorClock).hashCode();
//        return new HashCodeBuilder(17, 37)
//                .append(methodName + arguments)
//                .toHashCode();
    }
}
