package org.wgx.payments.transaction;

import java.util.LinkedList;
import java.util.List;

public final class TransactionUtils {

    private static ThreadLocal<Boolean> isInTrsanction = new ThreadLocal<>();
    private static ThreadLocal<List<Runnable>> actions = new ThreadLocal<>();

    public static void begin() {
        actions.set(new LinkedList<>());
        isInTrsanction.set(true);
    }

    public static void end() {
        actions.remove();
        isInTrsanction.set(false);
    }

    public static boolean isInTransaction() {
        return isInTrsanction.get();
    }

    public static void addAction(final Runnable action) {
        actions.get().add(action);
    }

    public static void doActions() {
        actions.get().parallelStream()
            .forEach(action -> {
                action.run();
            });
    }
}