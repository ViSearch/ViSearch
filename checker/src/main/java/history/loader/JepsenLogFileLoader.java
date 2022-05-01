package history.loader;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class JepsenLogFileLoader extends FileHistoryLoader {
    protected void loadFile(String filepath) {
        try {
            if (filepath.endsWith(".edn")) {
                BufferedReader br = new BufferedReader(new FileReader(filepath));
                String temp;
                List<List<JepsenOperation>> jepsenHistory = new ArrayList<>(16);
                while ((temp = br.readLine()) != null) {
                    JepsenOperation jepsenOperation = parseOperationString(temp);
                    while (jepsenHistory.size() <= jepsenOperation.process) {
                        jepsenHistory.add(new ArrayList<>());
                    }
                    jepsenHistory.get(jepsenOperation.process.intValue()).add(jepsenOperation);
                }

                for (List<JepsenOperation> jepsenOperationList : jepsenHistory) {
                    List<PlainOperation> records = new ArrayList<>();
                    for (int i = 0; i < jepsenOperationList.size(); i = i + 2) {
                        PlainOperation record = generatePlainOperation(jepsenOperationList.get(i), jepsenOperationList.get(i + 1));
                        if (record != null) {
                            records.add(record);
                        }
                    }
                    plainHistory.add(records);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private JepsenOperation parseOperationString(String str) {
        JepsenOperation operation = new JepsenOperation();
        str = str.substring(1, str.length() - 1);
        String[] terms = str.split(", ");
        for (int i = 0; i < terms.length; i++) {
            if (i == 0) {
                int quote = terms[i].lastIndexOf(':');
                operation.type = terms[i].substring(quote + 1);
            } else if (i == 1) {
                int quote = terms[i].lastIndexOf(':');
                operation.methodName = terms[i].substring(quote + 1);
            } else if (i == 2) {
                int space = terms[i].indexOf(' ');
                if (operation.type.equals("invoke")) {
                    String[] valueList = terms[i].substring(space + 2, terms[i].length() - 1).split(" ");
                    for (int k = 2; k < valueList.length; k++) {
                        operation.values.add(valueList[k]);
                    }
                } else if (operation.type.equals("ok")) {
                    String value = terms[i].substring(space + 1);
                    if (value.equals("nil") || value.equals("[]") || value.equals("[nil]")) {
                        operation.values.add("null");
                    } else {
                        operation.values.add(value.substring(1, value.length() - 1));
                    }
                }
            } else if (i == 4) {
                int space = terms[i].lastIndexOf(' ');
                operation.process = Long.parseLong(terms[i].substring(space + 1));
            } else if (i == 5) {
                int space = terms[i].lastIndexOf(' ');
                operation.index = Long.parseLong(terms[i].substring(space + 1));
            }
        }
        return operation;
    }

    private PlainOperation generatePlainOperation(JepsenOperation invoke, JepsenOperation ok) {
        if (ok.type.equals("fail")) {
            return null;
        }
        String methodName = invoke.methodName;
        List<String> retValues = ok.values;
        List<String> arguments = invoke.values;
        return new PlainOperation(methodName, arguments, retValues, 0, 0);
    }
}
