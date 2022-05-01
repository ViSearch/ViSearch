package checking;

import clojure.lang.Iterate;
import clojure.lang.PersistentArrayMap;
import clojure.lang.PersistentVector;
import datatype.AbstractDataType;
import datatype.DataTypeFactory;
import history.HappenBeforeGraph;
import history.Program;
import history.VisibilityType;
import traceprocessing.JepsenHistoryProcessor;

import java.util.Map;

public class JChecker extends VisearchChecker {
    public JChecker(String adt) {
        super(adt, 1, false);
    }

    public String visCheck(String history) {
        HappenBeforeGraph happenBeforeGraph = load(history);
//        System.out.println(happenBeforeGraph);
        return check(happenBeforeGraph);
    }

    public String test(VSModel model, String input) {
       return "";
    }

    public String visCheckForJepsen(PersistentVector history) {
        HappenBeforeGraph happenBeforeGraph = new JepsenHistoryProcessor().transformHistory(history);
        return check(happenBeforeGraph);
    }

    protected HappenBeforeGraph load(String history) {
        JepsenHistoryProcessor processor = new JepsenHistoryProcessor();
        return new HappenBeforeGraph(processor.generateProgram(history, DataTypeFactory.getInstance().getDataType(adt)));
    }

    public String check(HappenBeforeGraph history) {
        removeDummyOperations(history);
        try {
            for (int i = 0; i < 6; i++) {
                boolean result = testTrace(history, VisibilityType.values()[i]);
                if (result) {
                    return VisibilityType.values()[i].name();
                }
            }
            return "undefined";
        } catch (Exception e) {
            e.printStackTrace();
            return "exception";
        }
    }
}
