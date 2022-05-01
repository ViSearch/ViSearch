package history.loader;

import checking.JChecker;
import clojure.lang.Keyword;
import clojure.lang.PersistentArrayMap;
import clojure.lang.PersistentVector;
import history.HappenBeforeGraph;
import history.Invocation;
import history.Program;

import java.util.ArrayList;
import java.util.List;

public class JepsenObjectHistoryTransformer extends ObjectHistoryTransformer {



//    protected void load(String history) {
//        List<JepsenOperation> jepsenOperations = splitHistory(history);
//        List<List<JepsenOperation>> jepsenHistory = new ArrayList<>(16);
//        for (JepsenOperation jepsenOperation : jepsenOperations) {
//            while (jepsenHistory.size() <= jepsenOperation.process) {
//                jepsenHistory.add(new ArrayList<>());
//            }
//            jepsenHistory.get(jepsenOperation.process.intValue()).add(jepsenOperation);
//        }
//
//        for (List<JepsenOperation> jepsenOperationList : jepsenHistory) {
//            List<PlainOperation> records = new ArrayList<>();
//            for (int i = 0; i < jepsenOperationList.size(); i = i + 2) {
//                PlainOperation record = generateRecord(jepsenOperationList.get(i), jepsenOperationList.get(i + 1));
//                if (record != null) {
//                    records.add(record);
//                }
//            }
//            plainHistory.add(records);
//        }
//    }

    @Override
    public HappenBeforeGraph transformHistory(Object objectHistory) {
        PersistentVector history = (PersistentVector) objectHistory;
        List<List<JepsenOperation>> jepsenHistory = new ArrayList<>(16);
        for (int i = 0; i < history.length(); i++) {
            PersistentArrayMap invocationFromJepsen = (PersistentArrayMap) history.get(i);
            JepsenOperation jepsenOperation = parse(invocationFromJepsen);
            while (jepsenHistory.size() <= jepsenOperation.process) {
                jepsenHistory.add(new ArrayList<>());
            }
            jepsenHistory.get(jepsenOperation.process.intValue()).add(jepsenOperation);
        }

        List<List<Invocation>> program = new ArrayList<>();
        for (List<JepsenOperation> jepsenOperationList : jepsenHistory) {
            List<Invocation> subProgram = new ArrayList<>();
            for (int i = 0; i < jepsenOperationList.size(); i = i + 2) {
                Invocation invocation = generateInvocation(jepsenOperationList.get(i), jepsenOperationList.get(i + 1));
                if (invocation != null) {
                    subProgram.add(invocation);
                }
            }
            program.add(subProgram);
            System.out.println(subProgram);
        }
        return new HappenBeforeGraph(new Program(program));
    }

    protected JepsenOperation parse(PersistentArrayMap invocationFromJepsen) {
        JepsenOperation jepsenOperation = new JepsenOperation();
        jepsenOperation.type = ((Keyword)invocationFromJepsen.valAt(Keyword.intern(null,"type"))).getName();
        jepsenOperation.methodName = ((Keyword)invocationFromJepsen.valAt(Keyword.intern(null,"f"))).getName();
        jepsenOperation.process = (Long)invocationFromJepsen.valAt(Keyword.intern(null,"process"));
        jepsenOperation.index = (Long)invocationFromJepsen.valAt(Keyword.intern(null,"index"));
        Object value = invocationFromJepsen.valAt(Keyword.intern(null,"value"), new ArrayList<>());
        if (value != null) {
            if (value instanceof PersistentVector) {
                jepsenOperation.arguments.addAll((PersistentVector) value);
            } else {
                jepsenOperation.arguments.add(value);
            }
        }
        return jepsenOperation;
    }

    private Invocation generateInvocation(JepsenOperation invoke, JepsenOperation ok) {
        if (ok.type.equals("fail")) {
            return null;
        }
        Invocation invocation = new Invocation();
        invocation.setMethodName(invoke.methodName);
        if (invoke.arguments != null) {
            for (Object o : invoke.arguments) {
                invocation.addArguments(o);
            }
        }
        invocation.setRetValues(ok.arguments);
        return invocation;
    }

//    private PlainOperation generateRecord(JepsenOperation invoke, JepsenOperation ok) {
//        if (ok.type.equals("fail")) {
//            return null;
//        }
//        String methodName = invoke.methodName;
//        List<String> retValues = ok.values;
//        List<String> arguments = invoke.values;
//        return new PlainOperation(methodName, arguments, retValues, 0, 0);
//    }
//
//    private List<JepsenOperation> splitHistory(String history) {
//        List<JepsenOperation> operations = new ArrayList<>();
//        String regex = "\\{([^}])*\\}";
//        Pattern pattern = Pattern.compile(regex);
//        Matcher matcher = pattern.matcher(history);
//        while (matcher.find()) {
//            String s = matcher.group();
//            operations.add(parseOperationString(s));
//        }
//        return operations;
//    }

//    private JepsenOperation parseOperationString(String str) {
//        JepsenOperation operation = new JepsenOperation();
//        str = str.substring(1, str.length() - 1);
//        String[] terms = str.split(", ");
//        for (int i = 0; i < terms.length; i++) {
//            if (i == 0) {
//                int quote = terms[i].lastIndexOf(':');
//                operation.type = terms[i].substring(quote + 1);
//            } else if (i == 1) {
//                int quote = terms[i].lastIndexOf(':');
//                operation.methodName = terms[i].substring(quote + 1);
//            } else if (i == 2) {
//                int space = terms[i].indexOf(' ');
//                if (operation.type.equals("invoke")) {
//                    String[] valueList = terms[i].substring(space + 2, terms[i].length() - 1).split(" ");
//                    for (int k = 2; k < valueList.length; k++) {
//                        operation.values.add(valueList[k]);
//                    }
//                } else if (operation.type.equals("ok")) {
//                    String value = terms[i].substring(space + 1);
//                    if (value.equals("nil") || value.equals("[]") || value.equals("[nil]")) {
//                        operation.values.add("null");
//                    } else {
//                        operation.values.add(value.substring(1, value.length() - 1));
//                    }
//                }
//            } else if (i == 4) {
//                int space = terms[i].lastIndexOf(' ');
//                operation.process = Long.parseLong(terms[i].substring(space + 1));
//            } else if (i == 5) {
//                int space = terms[i].lastIndexOf(' ');
//                operation.index = Long.parseLong(terms[i].substring(space + 1));
//            }
//        }
//        return operation;
//    }
}

class JepsenOperation {
    public String type;
    public String methodName;
    public List<String> values = new ArrayList<>();

    public List<Object> arguments = new ArrayList<>();
    public Long process;
    public Long index;

    public String toString() {
        String result = "";
        result += ":type ";
        result += ":" + type + ", ";
        result += ":f :" + this.methodName + ", ";
        result += ":value ";
        for (String value : values) {
            result += value + " ";
        }
        result += ", ";
        result += ":process " + process;
        result += ", :index " + index;
        return result;
    }
}

//[{:type :invoke, :f :score, :value ["rwfzscore" "default" 2], :time 17351649, :process 0, :index 0} {:type :ok, :f :score, :value [nil], :time 142633319, :process 0, :index 1} {:type :invoke, :f :rem, :value ["rwfzrem" "default" 4], :time 1644977481, :process 1, :index 2} {:type :ok, :f :rem, :value ["rwfzrem" "default" 4], :time 1648888318, :process 1, :index 3} {:type :invoke, :f :max, :value ["rwfzmax" "default"], :time 1859903329, :process 1, :index 4} {:type :ok, :f :max, :value [], :time 1862094802, :process 1, :index 5} {:type :invoke, :f :add, :value ["rwfzadd" "default" 2 51], :time 3007450342, :process 2, :index 6} {:type :ok, :f :add, :value nil, :time 3009686823, :process 2, :index 7} {:type :invoke, :f :incrby, :value ["rwfzincrby" "default" 3 72], :time 3745041880, :process 2, :index 8} {:type :ok, :f :incrby, :value ["rwfzincrby" "default" 3 72], :time 3747268035, :process 2, :index 9} {:type :invoke, :f :add, :value ["rwfzadd" "default" 1 16], :time 5056467355, :process 1, :index 10} {:type :ok, :f :add, :value nil, :time 5057724061, :process 1, :index 11} {:type :invoke, :f :rem, :value ["rwfzrem" "default" 2], :time 6460044791, :process 2, :index 12} {:type :ok, :f :rem, :value nil, :time 6462468647, :process 2, :index 13} {:type :invoke, :f :add, :value ["rwfzadd" "default" 0 52], :time 7068761105, :process 0, :index 14} {:type :ok, :f :add, :value nil, :time 7070220237, :process 0, :index 15} {:type :invoke, :f :incrby, :value ["rwfzincrby" "default" 4 -44], :time 8376577669, :process 1, :index 16} {:type :ok, :f :incrby, :value ["rwfzincrby" "default" 4 -44], :time 8378861721, :process 1, :index 17} {:type :invoke, :f :rem, :value ["rwfzrem" "default" 2], :time 8484572164, :process 1, :index 18} {:type :ok, :f :rem, :value ["rwfzrem" "default" 2], :time 8486530407, :process 1, :index 19} {:type :invoke, :f :score, :value ["rwfzscore" "default" 4], :time 10005312407, :process 1, :index 20} {:type :ok, :f :score, :value [nil], :time 10007527595, :process 1, :index 21} {:type :invoke, :f :rem, :value ["rwfzrem" "default" 0], :time 10200955264, :process 1, :index 22} {:type :ok, :f :rem, :value nil, :time 10202058491, :process 1, :index 23} {:type :invoke, :f :incrby, :value ["rwfzincrby" "default" 4 94], :time 11867378386, :process 1, :index 24} {:type :ok, :f :incrby, :value ["rwfzincrby" "default" 4 94], :time 11868646003, :process 1, :index 25} {:type :invoke, :f :score, :value ["rwfzscore" "default" 0], :time 12921307327, :process 1, :index 26} {:type :ok, :f :score, :value [nil], :time 12923181146, :process 1, :index 27} {:type :invoke, :f :rem, :value ["rwfzrem" "default" 4], :time 14125691798, :process 2, :index 28} {:type :ok, :f :rem, :value ["rwfzrem" "default" 4], :time 14127327841, :process 2, :index 29} {:type :invoke, :f :incrby, :value ["rwfzincrby" "default" 1 -73], :time 14241801304, :process 2, :index 30} {:type :ok, :f :incrby, :value nil, :time 14244410038, :process 2, :index 31} {:type :invoke, :f :max, :value ["rwfzmax" "default"], :time 15674461653, :process 1, :index 32} {:type :ok, :f :max, :value [1 -57], :time 15676402418, :process 1, :index 33} {:type :invoke, :f :add, :value ["rwfzadd" "default" 3 38], :time 16311377371, :process 2, :index 34} {:type :ok, :f :add, :value nil, :time 16312656302, :process 2, :index 35} {:type :invoke, :f :incrby, :value ["rwfzincrby" "default" 1 27], :time 17129202091, :process 0, :index 36} {:type :ok, :f :incrby, :value nil, :time 17130992658, :process 0, :index 37} {:type :invoke, :f :add, :value ["rwfzadd" "default" 4 57], :time 17686825244, :process 2, :index 38} {:type :ok, :f :add, :value nil, :time 17688201276, :process 2, :index 39} {:type :invoke, :f :max, :value ["rwfzmax" "default"], :time 17896931347, :process 0, :index 40} {:type :ok, :f :max, :value [4 57], :time 17898661341, :process 0, :index 41} {:type :invoke, :f :add, :value ["rwfzadd" "default" 3 72], :time 19155399571, :process 1, :index 42} {:type :ok, :f :add, :value ["rwfzadd" "default" 3 72], :time 19156937642, :process 1, :index 43} {:type :invoke, :f :incrby, :value ["rwfzincrby" "default" 3 3], :time 19491988295, :process 0, :index 44} {:type :ok, :f :incrby, :value nil, :time 19493222353, :process 0, :index 45} {:type :invoke, :f :incrby, :value ["rwfzincrby" "default" 4 18], :time 20383005927, :process 2, :index 46} {:type :ok, :f :incrby, :value nil, :time 20384658429, :process 2, :index 47} {:type :invoke, :f :add, :value ["rwfzadd" "default" 3 67], :time 21496853466, :process 1, :index 48} {:type :ok, :f :add, :value ["rwfzadd" "default" 3 67], :time 21499030362, :process 1, :index 49} {:type :invoke, :f :max, :value ["rwfzmax" "default"], :time 21505784958, :process 2, :index 50} {:type :ok, :f :max, :value [4 75], :time 21506709252, :process 2, :index 51} {:type :invoke, :f :score, :value ["rwfzscore" "default" 4], :time 22121784071, :process 1, :index 52} {:type :ok, :f :score, :value [75], :time 22123803264, :process 1, :index 53} {:type :invoke, :f :rem, :value ["rwfzrem" "default" 4], :time 22751212401, :process 2, :index 54} {:type :ok, :f :rem, :value nil, :time 22752331303, :process 2, :index 55} {:type :invoke, :f :rem, :value ["rwfzrem" "default" 0], :time 23235232468, :process 2, :index 56} {:type :ok, :f :rem, :value ["rwfzrem" "default" 0], :time 23236814609, :process 2, :index 57} {:type :invoke, :f :incrby, :value ["rwfzincrby" "default" 1 12], :time 23409332402, :process 0, :index 58} {:type :ok, :f :incrby, :value nil, :time 23410576466, :process 0, :index 59} {:type :invoke, :f :max, :value ["rwfzmax" "default"], :time 23957992556, :process 1, :index 60} {:type :ok, :f :max, :value [3 41], :time 23959298943, :process 1, :index 61} {:type :invoke, :f :add, :value ["rwfzadd" "default" 2 56], :time 25069194930, :process 1, :index 62} {:type :ok, :f :add, :value nil, :time 25070902871, :process 1, :index 63} {:type :invoke, :f :score, :value ["rwfzscore" "default" 0], :time 26093053901, :process 0, :index 64} {:type :ok, :f :score, :value [nil], :time 26094518589, :process 0, :index 65} {:type :invoke, :f :incrby, :value ["rwfzincrby" "default" 2 91], :time 26354117887, :process 1, :index 66} {:type :ok, :f :incrby, :value nil, :time 26356011381, :process 1, :index 67} {:type :invoke, :f :score, :value ["rwfzscore" "default" 4], :time 26705728433, :process 1, :index 68} {:type :ok, :f :score, :value [nil], :time 26707138261, :process 1, :index 69} {:type :invoke, :f :add, :value ["rwfzadd" "default" 1 97], :time 27339328079, :process 1, :index 70} {:type :ok, :f :add, :value ["rwfzadd" "default" 1 97], :time 27341063829, :process 1, :index 71} {:type :invoke, :f :add, :value ["rwfzadd" "default" 4 47], :time 27506222673, :process 1, :index 72} {:type :ok, :f :add, :value nil, :time 27508015523, :process 1, :index 73} {:type :invoke, :f :incrby, :value ["rwfzincrby" "default" 0 12], :time 28463604791, :process 1, :index 74} {:type :ok, :f :incrby, :value ["rwfzincrby" "default" 0 12], :time 28465239487, :process 1, :index 75} {:type :invoke, :f :rem, :value ["rwfzrem" "default" 4], :time 29120356664, :process 1, :index 76} {:type :ok, :f :rem, :value nil, :time 29121812595, :process 1, :index 77}]