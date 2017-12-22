package org.wgx.payments.dao.service.impl;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

import javax.annotation.Resource;

import org.wgx.payments.builder.FastSearchTableItemBuilder;
import org.wgx.payments.client.api.helper.PaymentOperation;
import org.wgx.payments.client.api.helper.PaymentResponseStatus;
import org.wgx.payments.dao.FastSearchTableDAO;
import org.wgx.payments.dao.PaymentRequestDAO;
import org.wgx.payments.dao.PaymentResponseDAO;
import org.wgx.payments.dao.service.PaymentsDAOService;
import org.wgx.payments.model.FastSearchTableItem;
import org.wgx.payments.model.FastSearchTableItemStatus;
import org.wgx.payments.model.PaymentRequest;
import org.wgx.payments.model.PaymentResponse;
import org.wgx.payments.transaction.Transaction;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

/**
 * Default implementation of {@link PaymentsDAOService}.
 *
 */
@Setter
@Slf4j
public class PaymentsDAOServiceImpl implements PaymentsDAOService {

    @Resource
    private PaymentRequestDAO paymentRequestDAO;

    @Resource
    private PaymentResponseDAO paymentResponseDAO;

    @Resource(name = "fastSearchTableDAO")
    private FastSearchTableDAO fastSearchTableDAO;

    /**
     * {@inheritDoc}
     */
    @Transaction
    @Override
    public boolean updateRequestAndResponse(final List<PaymentRequest> requests, final List<PaymentResponse> responses,
            final FastSearchTableItem... items) {
        PaymentRequest targetRequest = requests.get(0);
        PaymentResponse targetResponse = responses.get(0);

        if (targetRequest.getPaymentOperationType().equals(PaymentOperation.CHARGE.operationType())
                && targetResponse.getStatus() == PaymentResponseStatus.SUCCESS.status()) {
            markChargeOperationAsSucceed(targetRequest);
        } else if (targetRequest.getPaymentOperationType().equals(PaymentOperation.SIGN.operationType())
                && targetResponse.getStatus() == PaymentResponseStatus.SUCCESS.status()) {
            markSignOperationAsSucceed(targetRequest);
        } else if (targetRequest.getPaymentOperationType().equals(PaymentOperation.RESCIND.operationType())
                && targetResponse.getStatus() == PaymentResponseStatus.SUCCESS.status()) {
            fastSearchTableDAO.deleteItemByKey("sign" + targetRequest.getCustomerID() + targetRequest.getBusiness());
        }

        for (PaymentRequest request : requests) {
            paymentRequestDAO.update(request);
        }

        for (PaymentResponse response : responses) {
            paymentResponseDAO.update(response);
        }

        for (FastSearchTableItem item : items) {
            fastSearchTableDAO.deleteItem(item.getId());
        }

        return true;
    }

    private void markChargeOperationAsSucceed(final PaymentRequest targetRequest) {
        FastSearchTableItem item = fastSearchTableDAO.find("charge" + targetRequest.getReferenceID() + targetRequest.getBusiness());
        if (item != null) {
            log.info("Received duplicated payment response, the reference has been paied before, will add this response to auto refund queue.");
            //Duplicated charge response, the "order" has been paid via another payment request, we'll have to refund to customer.
            FastSearchTableItem refundItem = FastSearchTableItemBuilder.builder()
                    .itemKey("refund")
                    .transactionID(targetRequest.getTransactionID())
                    .status(FastSearchTableItemStatus.PENDING.status())
                    .time(Timestamp.valueOf(LocalDateTime.now()))
                    .message("Auto Refund Item")
                    .build();
            fastSearchTableDAO.save(refundItem);
        } else {
            item = FastSearchTableItemBuilder.builder()
                    .itemKey("charge" + targetRequest.getReferenceID() + targetRequest.getBusiness())
                    .transactionID(targetRequest.getTransactionID())
                    .time(Timestamp.valueOf(LocalDateTime.now()))
                    .status(FastSearchTableItemStatus.PROCESSED.status())
                    .message("Succeed")
                    .build();
            fastSearchTableDAO.save(item);
        }
    }

    private void markSignOperationAsSucceed(final PaymentRequest targetRequest) {
        FastSearchTableItem item = fastSearchTableDAO.find("sign" + targetRequest.getCustomerID() + targetRequest.getBusiness());
        if (item != null) {
            log.info("Received duplicated payment response, the reference has been signed before, will add this response to auto rescind queue.");
            //Duplicated sign response, the "order" has been signed via another payment request, we'll have to refund to customer.
            FastSearchTableItem rescindItem = FastSearchTableItemBuilder.builder()
                    .itemKey("rescind")
                    .transactionID(targetRequest.getTransactionID())
                    .status(FastSearchTableItemStatus.PENDING.status())
                    .time(Timestamp.valueOf(LocalDateTime.now()))
                    .message("Auto Rescind Item")
                    .build();
            fastSearchTableDAO.save(rescindItem);
        } else {
            item = FastSearchTableItemBuilder.builder()
                    .itemKey("sign" + targetRequest.getCustomerID() + targetRequest.getBusiness())
                    .transactionID(targetRequest.getTransactionID())
                    .status(FastSearchTableItemStatus.PROCESSED.status())
                    .time(Timestamp.valueOf(LocalDateTime.now()))
                    .message("Succeed")
                    .build();
            fastSearchTableDAO.save(item);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Transaction
    @Override
    public boolean updateAutoRefundRecords(final FastSearchTableItem item, final PaymentResponse response) {
        return paymentResponseDAO.update(response) == 1 && fastSearchTableDAO.deleteItem(item.getId());
    }

    /**
     * {@inheritDoc}
     */
    @Transaction
    @Override
    public boolean updateRequestAndResponse(final PaymentRequest request, final PaymentResponse response) {
        return paymentRequestDAO.update(request) == 1
                && paymentResponseDAO.update(response) == 1;
    }

}
