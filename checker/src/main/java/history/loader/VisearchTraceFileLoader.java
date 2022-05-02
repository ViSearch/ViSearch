package history.loader;

import datatype.SetOperationTransformer;

import java.io.*;
import java.util.ArrayList;

public class VisearchTraceFileLoader extends FileHistoryLoader {
    protected void loadFile(String filepath) {
        try {
            if (filepath.endsWith(".trc")) {
                BufferedReader br = new BufferedReader(new FileReader(filepath));
                String temp = br.readLine();
                String[] header = temp.split(" ");
                int threadNum = Integer.parseInt(header[0]);
                for (int i = 0; i < threadNum; i++) {
                    ArrayList<PlainOperation> process = new ArrayList<>();
                    for (int j = 0; j < Integer.parseInt(header[i+1]); j++) {
                        temp = br.readLine();
                        PlainOperation operation = parseOperation(temp);
                        process.add(operation);
                    }
                    plainHistory.add(process);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private PlainOperation parseOperation(String s) {
        String[] cols = s.split(",");
        PlainOperation operation = new PlainOperation();
        operation.startTime = Long.parseLong(cols[0]);
        operation.endTime = Long.parseLong(cols[1]);
        operation.operationName = cols[2];
        operation.retValues.add(cols[cols.length - 1]);
        for (int i = 3; i < cols.length - 1; i++) {
            operation.arguments.add(cols[i]);
        }
        return operation;
    }

    public static void main(String[] args) {
        FileHistoryLoader loader = new VisearchTraceFileLoader();
        System.out.println(loader.generateProgram("test/riak_set_2.trc", new SetOperationTransformer()).generateHappenBeforeGraph());
    }
}
