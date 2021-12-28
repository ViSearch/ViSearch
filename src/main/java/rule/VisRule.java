package rule;

import util.NodePair;

import java.util.HashSet;

public class VisRule {
    private NodePair vis;   // (update, query)->vis, (query, update)->unvis
    private HashSet<NodePair> hbs;
}
