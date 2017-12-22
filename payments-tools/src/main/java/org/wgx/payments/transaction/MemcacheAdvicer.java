package org.wgx.payments.transaction;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.wgx.payments.exception.DAOFrameWorkException;

@Aspect
public class MemcacheAdvicer {

    @Around("@annotation(action)")
    public Object process(final ProceedingJoinPoint point, final WriteableAction action) throws Throwable {
        if (!TransactionUtils.isInTransaction()) {
            return point.proceed();
        } else {
            TransactionUtils.addAction(() -> {
                try {
                    point.proceed();
                } catch (Throwable e) {
                    throw new DAOFrameWorkException("Fail to execute memcache action", e);
                }
            });
            return new Boolean(true);
        }
    }
}
