package datatype;

import java.util.HashMap;

public class OperationTypes {
    public enum OPERATION_TYPE {UPDATE, QUERY};

    private HashMap<String, OPERATION_TYPE> operationTypes = new HashMap<>();

    public OPERATION_TYPE getOperationType(String methodName) {
        return operationTypes.get(methodName);
    }

    public void setOperationType(String methodName, OPERATION_TYPE operationType) {
        operationTypes.put(methodName, operationType);
    }
}
