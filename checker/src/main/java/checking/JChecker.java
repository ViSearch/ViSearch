package checking;

import clojure.lang.PersistentVector;
import datatype.AbstractDataType;
import datatype.DataTypeCreator;
import history.HappenBeforeGraph;
import history.VisibilityType;
import history.loader.JepsenObjectHistoryTransformer;

public class JChecker extends Checker {
    public JChecker(String adt) {
        super(adt, 1, false);
    }
    public JChecker(DataTypeCreator creator) {
        super(creator, 1, false);
    }

    public String visCheck(Object history) {
        HappenBeforeGraph happenBeforeGraph = new JepsenObjectHistoryTransformer().transformHistory(history);
        return measureSingleTrace(happenBeforeGraph);
    }
}
