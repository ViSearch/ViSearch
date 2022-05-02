package datatype;

import history.Invocation;
import history.loader.PlainOperation;
import history.loader.PlainOperationTransformer;

public class RpqOperationTransformer implements PlainOperationTransformer {
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
            case "incrby": {
                invocation.addArguments(Integer.parseInt(operation.arguments.get(0)));
                invocation.addArguments(Long.parseLong(operation.arguments.get(1)));
                break;
            }
            case "score": {
                invocation.addArguments(Integer.parseInt(operation.arguments.get(0)));
                if (!operation.retValues.get(0).equals("null")) {
                    invocation.addRetValue(Integer.parseInt(operation.retValues.get(0)));
                }
                break;
            }
            case "max": {
                if (!operation.retValues.get(0).equals("null")) {
                    invocation.addRetValue(Integer.parseInt(operation.retValues.get(0)));
                    invocation.addRetValue(Integer.parseInt(operation.retValues.get(1)));
                }
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
