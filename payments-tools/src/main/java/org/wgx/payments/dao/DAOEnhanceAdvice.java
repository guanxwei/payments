package org.wgx.payments.dao;

import java.lang.reflect.Method;
import java.util.Collection;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Aspect
public class DAOEnhanceAdvice {

    @Around("@annotation(action)")
    public Object process(final ProceedingJoinPoint point, final DAOMethod action) throws Throwable {
        BaseFrameWorkDao<?> dao = null;
        try {
             String method = point.getSignature().getName();
             dao = (BaseFrameWorkDao<?>) point.getTarget();
             if (method.equalsIgnoreCase(dao.getInsertMethod())) {
                 allocateIdIfNeeded(dao, point.getArgs());
             }
             return point.proceed(point.getArgs());
        } catch (Exception e) {
            throw e;
        } finally {
            if (dao != null) {
                dao.close();
            }
        }
    }

    private void allocateIdIfNeeded(final BaseFrameWorkDao<?> dao, final Object[] arguments) {
        try {
            Object object = arguments[0];
            if (object instanceof Collection<?>) {
                Collection<?> collection = (Collection<?>) object;
                if (collection.isEmpty()) {
                    return;
                }
                Object firstObject = collection.iterator().next();
                String id = dao.getId();
                Method getMethod = retriveGetMethod(firstObject, id);
                Method setMethod = retriveSetMethod(firstObject, id);

                for (Object target : collection) {
                    long value = (long) getMethod.invoke(target);
                    if (value == 0) {
                        setMethod.invoke(target, dao.allocatedID());
                    }
                }
            } else {
                String id = dao.getId();
                Method getMethod = retriveGetMethod(object, id);
                Method setMethod = retriveSetMethod(object, id);
                long value = (long) getMethod.invoke(object);
                if (value == 0) {
                    setMethod.invoke(object, dao.allocatedID());
                }
            }
        } catch (Exception e) {
            log.warn("Error happened", e);
        }
    }

    private Method retriveGetMethod(final Object object, final String id) {
        try {
            String methodName = getMethodName("get", id);
            return object.getClass().getMethod(methodName);
        } catch (Exception e) {
            log.warn("Failed to retrive method infomation", e);
            return null;
        }
    }

    private Method retriveSetMethod(final Object object, final String id) {
        try {
            String methodName = getMethodName("set", id);
            return object.getClass().getMethod(methodName, long.class);
        } catch (Exception e) {
            log.warn("Failed to retrive method infomation", e);
            return null;
        }
    }

    private String getMethodName(final String prefix, final String attribute) {
        return prefix + attribute.toUpperCase().substring(0, 1) + attribute.substring(1);
    }
}
