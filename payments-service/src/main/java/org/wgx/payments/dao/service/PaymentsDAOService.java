package org.wgx.payments.dao.service;

import java.util.List;

import org.wgx.payments.model.FastSearchTableItem;
import org.wgx.payments.model.PaymentRequest;
import org.wgx.payments.model.PaymentResponse;

/**
 * Base interface of Payments platform's DAO service.
 *
 */
public interface PaymentsDAOService {

    /**
     * Update both the request and response. Invoked when the 3P payment response is pushed back and
     * all the validation step passed.
     * @param requests Payment requests need to be updated.
     * @param responses Payment responses need to be created.
     * @param items Fast search items that needs to be deleted, optional.
     * @return Manipulation result.
     */
    boolean updateRequestAndResponse(final List<PaymentRequest> requests, final List<PaymentResponse> responses,
            final FastSearchTableItem... items);

    /**
     * Update auto refund job related response and its corresponding fast search table item.
     * @param item Fast search table item.
     * @param response Duplicated charge response.
     * @return Manipulation result.
     */
    boolean updateAutoRefundRecords(final FastSearchTableItem item, final PaymentResponse response);

    /**
     * Update both the request and response. Invoked by backfill system to trigger manually refund.
     * @param request Payment request need to be updated.
     * @param response Payment response need to be created.
     * @return Manipulation result.
     */
    boolean updateRequestAndResponse(final PaymentRequest request, final PaymentResponse response);
}
