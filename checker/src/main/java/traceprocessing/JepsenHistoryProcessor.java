package traceprocessing;

import checking.JChecker;
import clojure.java.api.Clojure;
import clojure.lang.Keyword;
import clojure.lang.PersistentArrayMap;
import clojure.lang.PersistentVector;
import datatype.RedisRpq;
import history.HBGNode;
import history.HappenBeforeGraph;
import history.Invocation;
import history.Program;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class JepsenHistoryProcessor extends TraceProcessor {
    protected void load(String history) {
        List<JepsenOperation> jepsenOperations = splitHistory(history);
        List<List<JepsenOperation>> jepsenHistory = new ArrayList<>(16);
        for (JepsenOperation jepsenOperation : jepsenOperations) {
            while (jepsenHistory.size() <= jepsenOperation.process) {
                jepsenHistory.add(new ArrayList<>());
            }
            jepsenHistory.get(jepsenOperation.process.intValue()).add(jepsenOperation);
        }

        for (List<JepsenOperation> jepsenOperationList : jepsenHistory) {
            List<Record> records = new ArrayList<>();
            for (int i = 0; i < jepsenOperationList.size(); i = i + 2) {
                Record record = generateRecord(jepsenOperationList.get(i), jepsenOperationList.get(i + 1));
                if (record != null) {
                    records.add(record);
                }
            }
            rawTrace.add(records);
        }
    }

    public HappenBeforeGraph transformHistory(PersistentVector history) {
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

    private Record generateRecord(JepsenOperation invoke, JepsenOperation ok) {
        if (ok.type.equals("fail")) {
            return null;
        }
        String methodName = invoke.methodName;
        String retValue = ok.values.get(0);
        List<String> arguments = invoke.values;
        return new Record(methodName, arguments, retValue);
    }

    private List<JepsenOperation> splitHistory(String history) {
        List<JepsenOperation> operations = new ArrayList<>();
        String regex = "\\{([^}])*\\}";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(history);
        while (matcher.find()) {
            String s = matcher.group();
            operations.add(parseOperationString(s));
        }
        return operations;
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

    public static void main(String[] args) {
        String s = "[{:type :invoke, :f :score, :value [\"rwfzscore\" \"default\" 0], :time 13372317, :process 0, :index 0} {:type :ok, :f :score, :value [nil], :time 167869105, :process 0, :index 1} {:type :invoke, :f :rem, :value [\"rwfzrem\" \"default\" 4], :time 260549344, :process 1, :index 2} {:type :fail, :f :rem, :value nil, :time 264654318, :process 1, :index 3} {:type :invoke, :f :rem, :value [\"rwfzrem\" \"default\" 3], :time 541264672, :process 2, :index 4} {:type :fail, :f :rem, :value nil, :time 544062627, :process 2, :index 5} {:type :invoke, :f :score, :value [\"rwfzscore\" \"default\" 3], :time 789131678, :process 0, :index 6} {:type :ok, :f :score, :value [nil], :time 791378302, :process 0, :index 7} {:type :invoke, :f :incrby, :value [\"rwfzincrby\" \"default\" 4 -39], :time 929728472, :process 1, :index 8} {:type :fail, :f :incrby, :value nil, :time 932497054, :process 1, :index 9} {:type :invoke, :f :incrby, :value [\"rwfzincrby\" \"default\" 4 -41], :time 1086388549, :process 1, :index 10} {:type :fail, :f :incrby, :value nil, :time 1088529693, :process 1, :index 11} {:type :invoke, :f :add, :value [\"rwfzadd\" \"default\" 2 99], :time 1216587854, :process 0, :index 12} {:type :ok, :f :add, :value nil, :time 1218782207, :process 0, :index 13} {:type :invoke, :f :rem, :value [\"rwfzrem\" \"default\" 0], :time 1542476082, :process 2, :index 14} {:type :fail, :f :rem, :value nil, :time 1544675193, :process 2, :index 15} {:type :invoke, :f :score, :value [\"rwfzscore\" \"default\" 3], :time 1789077850, :process 2, :index 16} {:type :ok, :f :score, :value [nil], :time 1791284474, :process 2, :index 17} {:type :invoke, :f :incrby, :value [\"rwfzincrby\" \"default\" 1 56], :time 2024807436, :process 0, :index 18} {:type :fail, :f :incrby, :value nil, :time 2027358127, :process 0, :index 19} {:type :invoke, :f :rem, :value [\"rwfzrem\" \"default\" 0], :time 2388366984, :process 1, :index 20} {:type :fail, :f :rem, :value nil, :time 2391409225, :process 1, :index 21} {:type :invoke, :f :add, :value [\"rwfzadd\" \"default\" 3 62], :time 2531208763, :process 2, :index 22} {:type :ok, :f :add, :value nil, :time 2532474618, :process 2, :index 23} {:type :invoke, :f :incrby, :value [\"rwfzincrby\" \"default\" 4 64], :time 2816833436, :process 2, :index 24} {:type :fail, :f :incrby, :value nil, :time 2818321855, :process 2, :index 25} {:type :invoke, :f :score, :value [\"rwfzscore\" \"default\" 4], :time 3121991111, :process 2, :index 26} {:type :ok, :f :score, :value [nil], :time 3123820764, :process 2, :index 27} {:type :invoke, :f :score, :value [\"rwfzscore\" \"default\" 4], :time 3388476706, :process 1, :index 28} {:type :ok, :f :score, :value [nil], :time 3390511249, :process 1, :index 29} {:type :invoke, :f :incrby, :value [\"rwfzincrby\" \"default\" 1 -96], :time 3760574134, :process 1, :index 30} {:type :fail, :f :incrby, :value nil, :time 3762076274, :process 1, :index 31} {:type :invoke, :f :add, :value [\"rwfzadd\" \"default\" 4 66], :time 3776559111, :process 2, :index 32} {:type :ok, :f :add, :value nil, :time 3777982948, :process 2, :index 33} {:type :invoke, :f :add, :value [\"rwfzadd\" \"default\" 3 83], :time 4171816521, :process 1, :index 34} {:type :fail, :f :add, :value nil, :time 4173806523, :process 1, :index 35} {:type :invoke, :f :add, :value [\"rwfzadd\" \"default\" 4 89], :time 4319967677, :process 1, :index 36} {:type :fail, :f :add, :value nil, :time 4323190301, :process 1, :index 37} {:type :invoke, :f :max, :value [\"rwfzmax\" \"default\"], :time 4642315183, :process 0, :index 38} {:type :ok, :f :max, :value [2 99], :time 4644731492, :process 0, :index 39} {:type :invoke, :f :score, :value [\"rwfzscore\" \"default\" 2], :time 4960627318, :process 2, :index 40} {:type :ok, :f :score, :value [99], :time 4961941588, :process 2, :index 41} {:type :invoke, :f :max, :value [\"rwfzmax\" \"default\"], :time 4976510154, :process 0, :index 42} {:type :ok, :f :max, :value [2 99], :time 4977966360, :process 0, :index 43} {:type :invoke, :f :add, :value [\"rwfzadd\" \"default\" 0 13], :time 5346305141, :process 0, :index 44} {:type :ok, :f :add, :value nil, :time 5347610426, :process 0, :index 45} {:type :invoke, :f :add, :value [\"rwfzadd\" \"default\" 4 96], :time 5532366468, :process 0, :index 46} {:type :fail, :f :add, :value nil, :time 5534131551, :process 0, :index 47} {:type :invoke, :f :add, :value [\"rwfzadd\" \"default\" 0 21], :time 5846026732, :process 2, :index 48} {:type :fail, :f :add, :value nil, :time 5847939251, :process 2, :index 49} {:type :invoke, :f :score, :value [\"rwfzscore\" \"default\" 1], :time 5926323800, :process 0, :index 50} {:type :ok, :f :score, :value [nil], :time 5927838240, :process 0, :index 51} {:type :invoke, :f :max, :value [\"rwfzmax\" \"default\"], :time 5974201558, :process 2, :index 52} {:type :ok, :f :max, :value [2 99], :time 5975597638, :process 2, :index 53} {:type :invoke, :f :incrby, :value [\"rwfzincrby\" \"default\" 3 76], :time 6181645392, :process 1, :index 54} {:type :ok, :f :incrby, :value nil, :time 6183811187, :process 1, :index 55} {:type :invoke, :f :score, :value [\"rwfzscore\" \"default\" 3], :time 6576579052, :process 2, :index 56} {:type :ok, :f :score, :value [138], :time 6577755593, :process 2, :index 57} {:type :invoke, :f :max, :value [\"rwfzmax\" \"default\"], :time 6910979969, :process 1, :index 58} {:type :ok, :f :max, :value [3 138], :time 6912215098, :process 1, :index 59} {:type :invoke, :f :add, :value [\"rwfzadd\" \"default\" 1 97], :time 7123827724, :process 2, :index 60} {:type :ok, :f :add, :value nil, :time 7125301814, :process 2, :index 61} {:type :invoke, :f :incrby, :value [\"rwfzincrby\" \"default\" 0 60], :time 7410269970, :process 2, :index 62} {:type :ok, :f :incrby, :value nil, :time 7412031571, :process 2, :index 63} {:type :invoke, :f :add, :value [\"rwfzadd\" \"default\" 1 96], :time 7726722674, :process 1, :index 64} {:type :fail, :f :add, :value nil, :time 7728410143, :process 1, :index 65} {:type :invoke, :f :add, :value [\"rwfzadd\" \"default\" 4 89], :time 7815342189, :process 1, :index 66} {:type :fail, :f :add, :value nil, :time 7816888726, :process 1, :index 67} {:type :invoke, :f :rem, :value [\"rwfzrem\" \"default\" 2], :time 7826002055, :process 0, :index 68} {:type :ok, :f :rem, :value nil, :time 7826849711, :process 0, :index 69} {:type :invoke, :f :score, :value [\"rwfzscore\" \"default\" 1], :time 8223580794, :process 1, :index 70} {:type :ok, :f :score, :value [97], :time 8225555964, :process 1, :index 71} {:type :invoke, :f :max, :value [\"rwfzmax\" \"default\"], :time 8236208469, :process 0, :index 72} {:type :ok, :f :max, :value [3 138], :time 8237120731, :process 0, :index 73} {:type :invoke, :f :rem, :value [\"rwfzrem\" \"default\" 1], :time 8262800124, :process 1, :index 74} {:type :ok, :f :rem, :value nil, :time 8263548472, :process 1, :index 75} {:type :invoke, :f :score, :value [\"rwfzscore\" \"default\" 0], :time 8324966719, :process 1, :index 76} {:type :ok, :f :score, :value [73], :time 8326114273, :process 1, :index 77} {:type :invoke, :f :add, :value [\"rwfzadd\" \"default\" 3 21], :time 8717166817, :process 2, :index 78} {:type :fail, :f :add, :value nil, :time 8718616236, :process 2, :index 79} {:type :invoke, :f :incrby, :value [\"rwfzincrby\" \"default\" 2 5], :time 9105753663, :process 2, :index 80} {:type :fail, :f :incrby, :value nil, :time 9108603782, :process 2, :index 81} {:type :invoke, :f :incrby, :value [\"rwfzincrby\" \"default\" 1 19], :time 9473891321, :process 1, :index 82} {:type :fail, :f :incrby, :value nil, :time 9475446088, :process 1, :index 83} {:type :invoke, :f :score, :value [\"rwfzscore\" \"default\" 3], :time 9727422318, :process 2, :index 84} {:type :ok, :f :score, :value [138], :time 9729503948, :process 2, :index 85} {:type :invoke, :f :max, :value [\"rwfzmax\" \"default\"], :time 9992383836, :process 2, :index 86} {:type :ok, :f :max, :value [3 138], :time 9994012523, :process 2, :index 87}]";
//        String s = "[{:type :invoke, :f :incrby, :value [\"rwfzincrby\" \"default\" 4 81], :time 19710158, :process 0, :index 0} {:type :ok, :f :incrby, :value nil, :time 145007842, :process 0, :index 1} {:type :invoke, :f :incrby, :value [\"rwfzincrby\" \"default\" 3 10], :time 438705454, :process 1, :index 2} {:type :ok, :f :incrby, :value nil, :time 441192738, :process 1, :index 3} {:type :invoke, :f :score, :value [\"rwfzscore\" \"default\" 1], :time 2306277924, :process 2, :index 4} {:type :ok, :f :score, :value [nil], :time 2309026955, :process 2, :index 5} {:type :invoke, :f :add, :value [\"rwfzadd\" \"default\" 3 34], :time 3508142907, :process 0, :index 6} {:type :ok, :f :add, :value nil, :time 3510864476, :process 0, :index 7} {:type :invoke, :f :max, :value [\"rwfzmax\" \"default\"], :time 4943031868, :process 1, :index 8} {:type :ok, :f :max, :value [3 34], :time 4945509094, :process 1, :index 9} {:type :invoke, :f :max, :value [\"rwfzmax\" \"default\"], :time 6554368471, :process 0, :index 10} {:type :ok, :f :max, :value [3 34], :time 6555637042, :process 0, :index 11} {:type :invoke, :f :max, :value [\"rwfzmax\" \"default\"], :time 7608870931, :process 1, :index 12} {:type :ok, :f :max, :value [3 34], :time 7610482218, :process 1, :index 13} {:type :invoke, :f :score, :value [\"rwfzscore\" \"default\" 4], :time 8455890380, :process 1, :index 14} {:type :ok, :f :score, :value [nil], :time 8457599096, :process 1, :index 15} {:type :invoke, :f :score, :value [\"rwfzscore\" \"default\" 4], :time 9056895079, :process 1, :index 16} {:type :ok, :f :score, :value [nil], :time 9058680923, :process 1, :index 17} {:type :invoke, :f :rem, :value [\"rwfzrem\" \"default\" 0], :time 10255502851, :process 2, :index 18} {:type :ok, :f :rem, :value nil, :time 10257406073, :process 2, :index 19} {:type :invoke, :f :add, :value [\"rwfzadd\" \"default\" 4 22], :time 10542925036, :process 0, :index 20} {:type :ok, :f :add, :value nil, :time 10544659708, :process 0, :index 21} {:type :invoke, :f :incrby, :value [\"rwfzincrby\" \"default\" 0 75], :time 11197858998, :process 1, :index 22} {:type :ok, :f :incrby, :value nil, :time 11199502428, :process 1, :index 23} {:type :invoke, :f :max, :value [\"rwfzmax\" \"default\"], :time 11530457940, :process 2, :index 24} {:type :ok, :f :max, :value [3 34], :time 11532049830, :process 2, :index 25} {:type :invoke, :f :add, :value [\"rwfzadd\" \"default\" 1 59], :time 11545880079, :process 2, :index 26} {:type :ok, :f :add, :value nil, :time 11546704533, :process 2, :index 27} {:type :invoke, :f :incrby, :value [\"rwfzincrby\" \"default\" 2 -47], :time 12920178769, :process 2, :index 28} {:type :ok, :f :incrby, :value nil, :time 12921778942, :process 2, :index 29} {:type :invoke, :f :add, :value [\"rwfzadd\" \"default\" 1 38], :time 13569855796, :process 0, :index 30} {:type :ok, :f :add, :value nil, :time 13571619304, :process 0, :index 31} {:type :invoke, :f :rem, :value [\"rwfzrem\" \"default\" 2], :time 15305348015, :process 0, :index 32} {:type :ok, :f :rem, :value nil, :time 15307350034, :process 0, :index 33} {:type :invoke, :f :rem, :value [\"rwfzrem\" \"default\" 0], :time 17060823002, :process 2, :index 34} {:type :ok, :f :rem, :value nil, :time 17063326918, :process 2, :index 35} {:type :invoke, :f :score, :value [\"rwfzscore\" \"default\" 3], :time 17329007419, :process 2, :index 36} {:type :ok, :f :score, :value [34], :time 17331275245, :process 2, :index 37} {:type :invoke, :f :incrby, :value [\"rwfzincrby\" \"default\" 2 -69], :time 18694182820, :process 0, :index 38} {:type :ok, :f :incrby, :value nil, :time 18696014207, :process 0, :index 39} {:type :invoke, :f :incrby, :value [\"rwfzincrby\" \"default\" 1 -33], :time 20086757988, :process 1, :index 40} {:type :ok, :f :incrby, :value nil, :time 20088542882, :process 1, :index 41} {:type :invoke, :f :max, :value [\"rwfzmax\" \"default\"], :time 20859142966, :process 1, :index 42} {:type :ok, :f :max, :value [3 34], :time 20860775089, :process 1, :index 43} {:type :invoke, :f :score, :value [\"rwfzscore\" \"default\" 4], :time 21909185112, :process 1, :index 44} {:type :ok, :f :score, :value [22], :time 21910665693, :process 1, :index 45} {:type :invoke, :f :incrby, :value [\"rwfzincrby\" \"default\" 0 89], :time 23461320518, :process 1, :index 46} {:type :ok, :f :incrby, :value nil, :time 23462881269, :process 1, :index 47} {:type :invoke, :f :score, :value [\"rwfzscore\" \"default\" 1], :time 24927445405, :process 0, :index 48} {:type :ok, :f :score, :value [26], :time 24929442119, :process 0, :index 49} {:type :invoke, :f :score, :value [\"rwfzscore\" \"default\" 2], :time 25585423978, :process 0, :index 50} {:type :ok, :f :score, :value [nil], :time 25586815686, :process 0, :index 51} {:type :invoke, :f :rem, :value [\"rwfzrem\" \"default\" 1], :time 26388602863, :process 1, :index 52} {:type :ok, :f :rem, :value nil, :time 26390517545, :process 1, :index 53} {:type :invoke, :f :rem, :value [\"rwfzrem\" \"default\" 1], :time 28239344562, :process 0, :index 54} {:type :ok, :f :rem, :value nil, :time 28240994382, :process 0, :index 55} {:type :invoke, :f :score, :value [\"rwfzscore\" \"default\" 4], :time 28924224422, :process 1, :index 56} {:type :ok, :f :score, :value [22], :time 28925991203, :process 1, :index 57} {:type :invoke, :f :rem, :value [\"rwfzrem\" \"default\" 1], :time 29046526531, :process 0, :index 58} {:type :ok, :f :rem, :value nil, :time 29048099709, :process 0, :index 59} {:type :invoke, :f :incrby, :value [\"rwfzincrby\" \"default\" 2 -97], :time 29099898178, :process 0, :index 60} {:type :ok, :f :incrby, :value nil, :time 29101307413, :process 0, :index 61}]\n";
        System.out.println(new JChecker("rpq").visCheck(s));
    }
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