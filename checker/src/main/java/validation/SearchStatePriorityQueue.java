package validation;

import java.util.*;
import java.util.concurrent.LinkedBlockingQueue;

public class SearchStatePriorityQueue {
    private int mode; //0: stack, 1: queue, 2: priority queue
    private List<SearchState> list = new LinkedList<>();

    public SearchStatePriorityQueue(int mode) {
        if (mode >= 0 && mode <= 2) {
            this.mode = mode;
        } else {
            this.mode = 0;
        }
    }

    public boolean offer(SearchState searchState) {
        if (mode == 0) {
            list.add(0, searchState);
            return true;
        } else {
            return list.add(searchState);
        }
    }

    public SearchState peek() {
        return list.get(0);
    }

    public SearchState poll() {
        SearchState state = list.get(0);
        list.remove(0);
        return state;
    }

    public boolean isEmpty() {
        return list.isEmpty();
    }

    public int size() {
        return list.size();
    }

    public String toString() {
        return list.toString();
    }

}
