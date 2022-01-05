package history;

import java.util.ArrayList;
import java.util.List;

public class Program {
    private List<List<Invocation>> subPrograms = new ArrayList<>();

    public Program() {
        ;
    }

    public Program(List<List<Invocation>> subPrograms) {
        this.subPrograms = subPrograms;
    }

    public List<List<Invocation>> getSubPrograms() {
        return subPrograms;
    }

    public HappenBeforeGraph generateHappenBeforeGraph() {
        return new HappenBeforeGraph(this);
    }
}