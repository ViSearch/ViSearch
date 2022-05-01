package checking;

import clojure.lang.PersistentVector;
import history.HappenBeforeGraph;
import history.VisibilityType;
import history.loader.JepsenObjectHistoryTransformer;

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
        HappenBeforeGraph happenBeforeGraph = new JepsenObjectHistoryTransformer().transformHistory(history);
        return check(happenBeforeGraph);
    }

//    protected HappenBeforeGraph load(String history) {
//        JepsenObjectHistoryLoader processor = new JepsenObjectHistoryLoader();
//        return new HappenBeforeGraph(processor.generateProgram(history, DataTypeFactory.getInstance().getDataType(adt)));
//    }

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
