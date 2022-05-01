package datatype;

import java.util.HashMap;
import java.util.Map;

public class DataTypeFactory {

    private Map<String, Class> factory = new HashMap<>();

    private static DataTypeFactory instance = new DataTypeFactory();

    private DataTypeFactory() {
        factory.put("set", RiakSet.class);
        factory.put("map", RiakMap.class);
        factory.put("rpq", RedisRpq.class);
    }

    public static DataTypeFactory getInstance() {
        return instance;
    }

    public AbstractDataType getDataType(String dataType) {
        Class clazz = factory.get(dataType);
        if (clazz == null)
        {
            return null;
        } else {
            try {
                return (AbstractDataType) clazz.newInstance();
            } catch (Exception e) {
                return null;
            }
        }
    }
}
