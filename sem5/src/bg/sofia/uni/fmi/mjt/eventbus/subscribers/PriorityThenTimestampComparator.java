package bg.sofia.uni.fmi.mjt.eventbus.subscribers;

import bg.sofia.uni.fmi.mjt.eventbus.events.Event;

import java.util.Comparator;

public class PriorityThenTimestampComparator<T extends Event<?>> implements Comparator<T> {
    @Override
    public int compare(T a, T b) {
        int byPriority = Integer.compare(a.getPriority(), b.getPriority());
        if (byPriority != 0) {
            return byPriority;
        }
        return a.getTimestamp().compareTo(b.getTimestamp());
    }
}
