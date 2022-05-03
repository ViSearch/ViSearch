package datatype;

import java.util.HashMap;
import java.util.Map;

public class DataTypeFactory {

    private Map<String, DataTypeCreator> factory = new HashMap<>();

    private static DataTypeFactory instance = new DataTypeFactory();

    private DataTypeFactory() {
        factory.put("set", new CrdtSetCreator());
        factory.put("map", new CrdtMapCreator());
        factory.put("rpq", new CrdtRpqCreator());
    }

    public static DataTypeFactory getInstance() {
        return instance;
    }

    public AbstractDataType getDataType(String dataType) {
        DataTypeCreator creator = factory.get(dataType);
        if (creator == null)
        {
            return null;
        } else {
            return creator.createDataType();
        }
    }

    public void addDataType(String name, DataTypeCreator creator) {
        factory.put(name, creator);
    }
}

class CrdtSetCreator implements DataTypeCreator {
    @Override
    public AbstractDataType createDataType() {
        return new CrdtSet();
    }
}

class CrdtMapCreator implements DataTypeCreator {
    @Override
    public AbstractDataType createDataType() {
        return new CrdtMap();
    }
}

class CrdtRpqCreator implements DataTypeCreator {
    @Override
    public AbstractDataType createDataType() {
        return new CrdtRpq();
    }
}
