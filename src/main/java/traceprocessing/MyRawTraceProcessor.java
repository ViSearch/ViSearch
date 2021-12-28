package traceprocessing;

import datatype.RiakSet;

import java.io.*;
import java.util.ArrayList;

public class MyRawTraceProcessor extends TraceProcessor {
    protected void load(String filepath) {
        try {
            if (filepath.endsWith(".trc")) {
                BufferedReader br = new BufferedReader(new FileReader(filepath));
                String temp = br.readLine();
                String[] header = temp.split(" ");
                int threadNum = Integer.parseInt(header[0]);
                for (int i = 0; i < threadNum; i++) {
                    ArrayList<Record> thread = new ArrayList<>();
                    for (int j = 0; j < Integer.parseInt(header[i+1]); j++) {
                        temp = br.readLine();
                        Record record = new Record(temp);
                        thread.add(record);
                    }
                    rawTrace.add(thread);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        new MyRawTraceProcessor().generateProgram("test.trc", new RiakSet());
    }
}
