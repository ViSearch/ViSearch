package history.loader;

import history.HappenBeforeGraph;

public abstract class ObjectHistoryTransformer {
    public abstract HappenBeforeGraph transformHistory(Object objectHistory);
}
