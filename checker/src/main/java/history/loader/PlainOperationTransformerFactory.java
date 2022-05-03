package history.loader;

import datatype.AbstractDataType;
import datatype.MapOperationTransformer;
import datatype.RpqOperationTransformer;
import datatype.SetOperationTransformer;

import java.util.HashMap;
import java.util.Map;

public class PlainOperationTransformerFactory {
    private Map<String, Class> factory = new HashMap<>();

    public static PlainOperationTransformerFactory instance = new PlainOperationTransformerFactory();

    private PlainOperationTransformerFactory() {
        factory.put("set", SetOperationTransformer.class);
        factory.put("map", MapOperationTransformer.class);
        factory.put("rpq", RpqOperationTransformer.class);
    }

    public PlainOperationTransformer getTransformer(String dataType) {
        Class clazz = factory.get(dataType);
        if (clazz == null)
        {
            return null;
        } else {
            try {
                return (PlainOperationTransformer) clazz.newInstance();
            } catch (Exception e) {
                return null;
            }
        }
    }

    public void addTransformer(String name, Class transformer) {
        factory.put(name, transformer);
    }

    public static PlainOperationTransformerFactory getInstance() {
        return instance;
    }
}
