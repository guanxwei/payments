package org.wgx.payments.transaction;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Aspect
public class TransactionAspect {

    @Setter
    private TransactionManager transactionManager;

    @Around("@annotation(transaction)")
    public Object arround(final ProceedingJoinPoint point, final Transaction transaction) throws Throwable {
        if (transactionManager.getAutoCommit()) {
            try {
                transactionManager.setAutoCommit(false);
                TransactionUtils.begin();
                Object result = point.proceed();
                TransactionUtils.doActions();
                transactionManager.commit();
                TransactionUtils.end();
                return result;
            } catch (Exception e) {
                System.out.println("rollback transaction");
                log.error("Rollback db action due to error", e);
                transactionManager.rollback();
                throw e;
            } finally {
                transactionManager.releaseConnection();
            }
        } else {
            // 已经在事物处理中.
            return point.proceed();
        }

    }
}
