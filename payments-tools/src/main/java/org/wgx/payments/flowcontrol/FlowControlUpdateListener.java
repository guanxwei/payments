package org.wgx.payments.flowcontrol;

import java.util.HashMap;
import java.util.Map;

import org.wgx.payments.event.Event;
import org.wgx.payments.event.Listener;

import lombok.Setter;

public class FlowControlUpdateListener implements Listener {

    @Setter
    private Map<String, FlowMonitor> monitors = new HashMap<>();

    /**
     * {@inheritDoc}
     */
    @Override
    public void handle(final Event<?, ?> event) {
        if (event instanceof FlowControlUpdateEvent) {
            String config = (String) event.getObject();
            String key = (String) event.getSource();
            FlowMonitor monitor = monitors.get(key);
            monitor.reboot(Integer.parseInt(config));
        }
    }

}
