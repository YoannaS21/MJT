package bg.sofia.uni.fmi.mjt.eventbus;

import bg.sofia.uni.fmi.mjt.eventbus.events.Event;
import bg.sofia.uni.fmi.mjt.eventbus.exception.MissingSubscriptionException;
import bg.sofia.uni.fmi.mjt.eventbus.subscribers.Subscriber;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

public class EventBusImpl implements EventBus {
    private Map<Class<? extends Event<?>>, Collection<Subscriber<?>>> eventSubscribers;
    private Map<Class<? extends Event<?>>, Collection<Event<?>>> eventLogs;

    public EventBusImpl() {
        this.eventSubscribers = new HashMap<>();
        this.eventLogs = new HashMap<>();
    }

    /**
     * Subscribes the given subscriber to the given event type.
     * If the same subscriber is already subscribed to the given event type, the method
     * should do nothing (no duplicate subscriptions).
     *
     * @param eventType  the type of event to subscribe to
     * @param subscriber the subscriber to subscribe
     * @throws IllegalArgumentException if the event type is null
     * @throws IllegalArgumentException if the subscriber is null
     */
    @Override
    public <T extends Event<?>> void subscribe(Class<T> eventType, Subscriber<? super T> subscriber) {
        if (eventType == null || subscriber == null) {
            throw new IllegalArgumentException("Event type or subscriber is null.");
        }

        Collection<Subscriber<?>> subscribersForEvent;
        if (eventSubscribers.containsKey(eventType)) {
            subscribersForEvent = eventSubscribers.get(eventType);
        } else {
            subscribersForEvent = new HashSet<>();
            eventSubscribers.put(eventType, subscribersForEvent);
        }
        subscribersForEvent.add(subscriber);
    }

    /**
     * Unsubscribes the given subscriber from the given event type.
     *
     * @param eventType  the type of event to unsubscribe from
     * @param subscriber the subscriber to unsubscribe
     * @throws IllegalArgumentException     if the event type is null
     * @throws IllegalArgumentException     if the subscriber is null
     * @throws MissingSubscriptionException if the subscriber is not subscribed to the event type
     */
    @Override
    public <T extends Event<?>> void unsubscribe(Class<T> eventType, Subscriber<? super T> subscriber)
            throws MissingSubscriptionException {
        if (eventType == null || subscriber == null) {
            throw new IllegalArgumentException("Either event type or subscriber is null");
        }

        Collection<Subscriber<?>> subscribers = eventSubscribers.get(eventType);
        if (subscribers == null || !subscribers.contains(subscriber)) {
            throw new MissingSubscriptionException("Can't unsubscribe when not subscribed.");
        }
        subscribers.remove(subscriber);
    }

    /**
     * Publishes the given event to all subscribers of the event type.
     *
     * @param event the event to publish
     * @throws IllegalArgumentException if the event is null
     */
    @Override
    public <T extends Event<?>> void publish(T event) {
        if (event == null) {
            throw new IllegalArgumentException("The event is null.");
        }
        //getClass will return <Class<? extends Object>> and not extends Event<?>, so we need to cast
        
        Class<? extends Event<?>> eventType = (Class<? extends Event<?>>) event.getClass();
        List<Event<?>> logs;

        if (eventLogs.containsKey(eventType)) {
            logs = (List<Event<?>>) eventLogs.get(eventType);
        } else {
            logs = new ArrayList<>();
            eventLogs.put(eventType, logs);
        }

        logs.add(event);

        Collection<Subscriber<?>> subscribers = eventSubscribers.get(event.getClass());
        if (subscribers != null) {
            for (Subscriber<?> subscriber : subscribers) {
                //We have Subscriber<?>, but we need to have <? super T> to access the onEvent func
                // the cast is safe because we subscribe only <? super T> elements
                Subscriber<? super T> castedSubscriber = (Subscriber<? super T>) subscriber;
                castedSubscriber.onEvent(event);
            }
        }
    }

    /**
     * Clears all subscribers and event logs.
     */
    @Override
    public void clear() {
        eventSubscribers.clear();
        eventLogs.clear();
    }

    @Override
    public Collection<? extends Event<?>> getEventLogs(Class<? extends Event<?>> eventType, Instant from, Instant to) {
        if (eventType == null || from == null || to == null) {
            throw new IllegalArgumentException("Event type or instant is null.");
        }
        Collection<Event<?>> logs = eventLogs.get(eventType);
        if (logs == null) {
            return new ArrayList<>();
        }
        List<Event<?>> inBoundaries = new ArrayList<>();
        for (Event<?> event : logs) {
            if (!event.getTimestamp().isBefore(from)
                    && event.getTimestamp().isBefore(to)) {
                inBoundaries.add(event);
            }
        }
        return Collections.unmodifiableList(inBoundaries);
    }

    @Override
    public <T extends Event<?>> Collection<Subscriber<?>> getSubscribersForEvent(Class<T> eventType) {
        if (eventType == null) {
            throw new IllegalArgumentException("Event type is null.");
        }

        Collection<Subscriber<?>> subscribers = eventSubscribers.get(eventType);
        if (subscribers == null) {
            return Collections.emptyList();
        }
        return Collections.unmodifiableCollection(subscribers);
    }
}
