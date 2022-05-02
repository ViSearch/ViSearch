package history.loader;

import history.Invocation;

public interface PlainOperationTransformer {
    public Invocation generateInvocation(PlainOperation operation);
}