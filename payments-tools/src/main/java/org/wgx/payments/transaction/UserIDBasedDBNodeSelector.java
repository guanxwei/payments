package org.wgx.payments.transaction;

import java.util.List;

import javax.sql.DataSource;

public class UserIDBasedDBNodeSelector implements DBNodeSelector {

    private static final ThreadLocal<Object> USER_INFO = new ThreadLocal<Object>();

    /**
     * Prepare user info so that this selector can select db node based on this info.
     * Every single user within the platform should always be located on the same node,
     * so the developers must ensure that the user instance's hashcode method return exactly the
     * same value.
     * @param user User identity, basically could be a long or a string.
     */
    public static void prepare(final Object user) {
        USER_INFO.set(user);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public DataSource select(final List<DataSource> dataSources) {
        if (USER_INFO.get() == null) throw new RuntimeException("User info should be prepared");

        int hashcode = USER_INFO.get().hashCode();

        return dataSources.get(hashcode % dataSources.size());
    }

}
