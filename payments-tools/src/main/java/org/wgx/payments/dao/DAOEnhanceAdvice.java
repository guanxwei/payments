package org.wgx.payments.dao;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;

@Aspect
public class DAOEnhanceAdvice {

    @Around("@annotation(action)")
    public Object process(final ProceedingJoinPoint point, final DAOMethod action) throws Throwable {
        BaseFrameWorkDao<?> dao = null;
        try {
             dao = (BaseFrameWorkDao<?>) point.getTarget();
             return point.proceed(point.getArgs());
        } catch (Exception e) {
            throw e;
        } finally {
            if (dao != null) {
                dao.close();
            }
        }
    }
}
