package org.wgx.payments.flowcontrol;

import java.util.HashMap;
import java.util.Map;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Spring Aspect intercepter that can be integrated with any system to help control the visiting flow rate.
 * @author hzweiguanxiong
 *
 */
@Aspect
public class FlowControlAspect {

    private static final Logger LOG = LoggerFactory.getLogger(FlowControlAspect.class);

    private Map<String, FlowMonitor> monitors = new HashMap<>();

    public void addMonitor(final String key, final FlowMonitor monitor) {
        monitors.put(key, monitor);
    }

    @Around("@annotation(flowControl)")
    public Object aroundMethod(final ProceedingJoinPoint pjd, final FlowControl flowControl) throws Throwable { 
        if (flowControl.key() == null) {
            throw new FlowControlException("Key must be specified if you want to integrate with flow control framework!");
        }
        FlowMonitor monitor = monitors.get(flowControl.key());
        if (monitor == null) {
            throw new FlowControlException("Can not find corresponding monitor to help control the visiting flow rate!");
        }

        if (monitor.enqueue()) {
            try {
                Object result = pjd.proceed();
                return result;
            } catch (Exception e) {
                LOG.error("Processing error", e);
                throw e;
            } finally {
                monitor.dequeue();
            }
        } else {
            throw new FlowControlException(flowControl.message());
        }
    }
}
