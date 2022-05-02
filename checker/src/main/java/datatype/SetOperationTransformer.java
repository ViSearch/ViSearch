package datatype;

import history.Invocation;
import history.loader.PlainOperation;
import history.loader.PlainOperationTransformer;

public class SetOperationTransformer implements PlainOperationTransformer {
    @Override
    public Invocation generateInvocation(PlainOperation operation) {
        Invocation invocation = new Invocation();
        for (Object arg : operation.arguments) {
            invocation.addArguments(arg);
        }
        invocation.setMethodName(operation.operationName);

        switch (operation.operationName) {
            case "add": {
                invocation.addArguments(Integer.parseInt(operation.arguments.get(0)));
                invocation.addArguments(Integer.parseInt(operation.arguments.get(1)));
                break;
            }
            case "rem": {
                invocation.addArguments(Integer.parseInt(operation.arguments.get(0)));
                break;
            }
            case "contains": {
                invocation.addArguments(Integer.parseInt(operation.arguments.get(0)));
                if (invocation.getRetValues().get(0).equals("false")) {
                    invocation.addRetValue(0);
                } else if (invocation.getRetValues().get(0).equals("true")) {
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
