package org.wgx.payments.flowcontrol;

import java.util.UUID;

import org.wgx.payments.event.Event;

public class FlowControlUpdateEvent implements Event<String, String> {

    private String source;
    private String object;
    private String eventID;

    public FlowControlUpdateEvent(final String source, final String object) {
        this.source = source;
        this.object = object;
        this.eventID = UUID.randomUUID().toString();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getSource() {
        return this.source;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getObject() {
        return this.object;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getEventID() {
        return this.eventID;
    }

}
