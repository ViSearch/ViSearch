package history.loader;

import datatype.AbstractDataType;
import history.Invocation;
import history.Program;

import java.util.ArrayList;
import java.util.List;

public abstract class FileHistoryLoader extends BaseHistoryLoader {
    protected List<List<PlainOperation>> plainHistory = new ArrayList<>();

    protected abstract void loadFile(String filePath);
    public Program generateProgram(String filepath, AbstractDataType adt) {
        loadFile(filepath);
        List<List<Invocation>> subPrograms = new ArrayList<>();
        for (int i = 0; i < plainHistory.size(); i++) {
            List<Invocation> invocations = new ArrayList<>();
            for (PlainOperation r : plainHistory.get(i)) {
                invocations.add(r.generateInvocation(adt));
            }
            subPrograms.add(invocations);
        }
        return new Program(subPrograms);
    }
}
