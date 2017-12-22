package org.wgx.payments.event;

import java.util.List;

/**
 * Event center to gather and dispatch events.
 * @author hzweiguanxiong
 *
 */
public interface EventCenter {

    /**
     * Fire an event asynchronously.
     * @param event Event to be fired.
     */
    void fireEvent(final Event<?, ?> event);

    /**
     * Fire an event synchronously.
     * @param event Event to be fired.
     * @throws Exception  Exception thrown when fail to communicate with kafka server.
     */
    void fireSyncEvent(final Event<?, ?> event) throws Exception;

    /**
     * Register the event listener.
     * @param event Event type that the listener interested in.
     * @param listener Event listener.
     */
    void registerListener(final Class<?> event, final Listener listener);

    /**
     * Remove a listener from the event center.
     * @param listener Listener to be removed.
     */
    void removeListener(final Listener listener);

    /**
     * Register a listener that is interested in multi kinds of events.
     * @param events Event class list the listener is interested in.
     * @param listener Listener to be registered.
     */
    void registerMutilChannelListerner(final List<Class<?>> events, final Listener listener);

    /**
     * Get listener list by event type.
     * @param type Event type.
     * @return Listener list.
     */
    List<Listener> getListenerListByEventType(final Class<?> type);
}
