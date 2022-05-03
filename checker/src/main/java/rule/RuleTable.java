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
}
