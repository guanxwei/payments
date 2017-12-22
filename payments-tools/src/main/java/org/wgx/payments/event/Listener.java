package org.wgx.payments.event;

/**
 * Event listener.
 * @author hzweiguanxiong
 *
 */
public interface Listener {

    /**
     * Process the event.
     * @param event Event to be processed.
     */
    void handle(final Event<?, ?> event);
}
