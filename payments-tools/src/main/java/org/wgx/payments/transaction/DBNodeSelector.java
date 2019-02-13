package org.wgx.payments.transaction;

import java.util.List;

import javax.sql.DataSource;

/**
 * Shared data base node selector.
 * @author weigu
 *
 */
public interface DBNodeSelector {

    /**
     * Select a data source for the current action.
     * @return Target datsource.
     */
    DataSource select(final List<DataSource> dataSources);

}
