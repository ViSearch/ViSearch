package datatype;

public class DataTypeFactory {
    public AbstractDataType getDataType(String dataType) {
        if (dataType.equals("set")) {
            return new RiakSet();
        } else if (dataType.equals("map")) {
            return new RiakMap();
        } else if (dataType.equals("rpq")) {
            return new RedisRpq();
        } else {
            return null;
        }
    }
}
