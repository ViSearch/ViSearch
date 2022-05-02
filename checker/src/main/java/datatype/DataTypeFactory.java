package datatype;

import java.util.HashMap;
import java.util.Map;

public class DataTypeFactory {

    private Map<String, Class> factory = new HashMap<>();

    private static DataTypeFactory instance = new DataTypeFactory();

    private DataTypeFactory() {
        factory.put("set", CrdtSet.class);
        factory.put("map", CrdtMap.class);
        factory.put("rpq", CrdtRpq.class);
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
