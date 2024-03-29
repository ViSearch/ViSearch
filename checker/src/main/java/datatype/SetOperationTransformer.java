package datatype;

import history.Invocation;
import history.loader.PlainOperation;
import history.loader.PlainOperationTransformer;

public class SetOperationTransformer implements PlainOperationTransformer {
    @Override
    public Invocation generateInvocation(PlainOperation operation) {
        Invocation invocation = new Invocation();
        invocation.setMethodName(operation.operationName);

        switch (operation.operationName) {
            case "add": {
                invocation.addArguments(Integer.parseInt(operation.arguments.get(0)));
                break;
            }
            case "remove": {
                invocation.addArguments(Integer.parseInt(operation.arguments.get(0)));
                break;
            }
            case "contains": {
                invocation.addArguments(Integer.parseInt(operation.arguments.get(0)));
                if (operation.retValues.get(0).equals("false")) {
                    invocation.addRetValue(0);
                } else if (operation.retValues.get(0).equals("true")) {
                    invocation.addRetValue(1);
                }
                break;
            }
            case "size": {
                invocation.addRetValue(Integer.parseInt(operation.retValues.get(0)));
                break;
            }
            default: {
                System.out.println("Unknown operation");
                return null;
            }
        }
        return invocation;
    }
}
