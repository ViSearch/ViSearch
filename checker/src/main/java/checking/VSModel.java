package checking;

import history.Invocation;

public interface VSModel {
    public boolean step(Invocation invocation);

    public void reset();
}
