package traceprocessing;

import datatype.AbstractDataType;
import history.Invocation;
import history.Program;

import java.util.ArrayList;
import java.util.List;

public abstract class TraceProcessor {
    protected List<List<Record>> rawTrace = new ArrayList<>();

    protected abstract void load(String filepath);

    public Program generateProgram(String filepath, AbstractDataType adt) {
        load(filepath);
        List<List<Invocation>> subPrograms = new ArrayList<>();
        for (int i = 0; i < rawTrace.size(); i++) {
            List<Invocation> invocations = new ArrayList<>();
            for (Record r : rawTrace.get(i)) {
                invocations.add(r.generateInvocation(adt));
            }
            subPrograms.add(invocations);
        }
        return new Program(subPrograms);
    }
}
