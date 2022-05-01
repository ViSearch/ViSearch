package history.loader;

import java.util.ArrayList;
import java.util.List;

class JepsenOperation {
    public String type;
    public String methodName;
    public List<String> values = new ArrayList<>();

    public List<Object> arguments = new ArrayList<>();
    public Long process;
    public Long index;

    public String toString() {
        String result = "";
        result += ":type ";
        result += ":" + type + ", ";
        result += ":f :" + this.methodName + ", ";
        result += ":value ";
        for (String value : values) {
            result += value + " ";
        }
        result += ", ";
        result += ":process " + process;
        result += ", :index " + index;
        return result;
    }
}
