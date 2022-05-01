package rule;

import checking.VisearchChecker;
import history.Linearization;
import com.google.common.collect.HashMultimap;
import history.HBGNode;
import util.NodePair;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Collection;
import java.util.Set;

public class RuleTable {
    private HashMultimap<HBGNode, HBGNode> linRules = HashMultimap.create();
    private HashMultimap<NodePair, NodePair> visRules = HashMultimap.create();

    public RuleTable() {
        ;
    }

    public void insertVisRuleBulk(HashMultimap<NodePair, NodePair> rules) {
        visRules.putAll(rules);
    }

    public void insertLinRuleBulk(HashMultimap<HBGNode, HBGNode> rules) {
        linRules.putAll(rules);
    }

    public boolean linearizationFilter(Linearization linearization, HBGNode node) {
        Collection<HBGNode> mustBefore = linRules.get(node);
        for (HBGNode n : mustBefore) {
            if (!linearization.contains(n)) {
                return false;
            }
        }
        return true;
    }

    public boolean visibilityFilter(Set<NodePair> lin, NodePair node) {
        if (!visRules.containsKey(node)) {
            return false;
        }
        Set<NodePair> musthb = visRules.get(node);
        for (NodePair pair : lin) {
            if (!musthb.contains(pair)) {
                return false;
            }
        }
        return true;
    }

    public int size() {
        return linRules.size() + visRules.size();
    }

    public static void main(String[] args) throws Exception {

        File baseFile = new File("D:\\set311_with_size\\result");
        String dataType = "set";
        if (baseFile.isFile() || !baseFile.exists()) {
            throw new FileNotFoundException();
        }
        File[] files = baseFile.listFiles();
        int i = 0;
        long a = 0;
        long b = 0;
        for (File file : files) {
//            VisearchChecker checker = new VisearchChecker("set", 1, false);
            VisearchChecker checker1 = new VisearchChecker("set", 4, true);
            i++;
            if (i == 10000) {
                break;
            }
//            if (!file.toString().equals("D:\\set311_with_size\\result\\set311_default_5_3_15_1634985181583.trc")) {
//                continue;
//            }
//            System.out.println(checker.measureVisibility(file.toString()));
//            System.out.println(checker1.measureVisibility(file.toString()));

            System.out.println(file.toString() + ":" + checker1.measureVisibility(file.toString()));
//            System.out.println(file.toString());
//            if (checker1.isStateFilter) {
//                checker.measureVisibility(file.toString());
//                System.out.println(checker.getAverageState() + "," + checker1.getAverageState());
//            }

        }
    }
}
