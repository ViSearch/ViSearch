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
}