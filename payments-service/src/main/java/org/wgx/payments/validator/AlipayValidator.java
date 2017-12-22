package org.wgx.payments.validator;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import javax.annotation.Resource;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.wgx.payments.client.api.helper.PaymentOperation;
import org.wgx.payments.client.api.io.CreateOrUpdatePaymentResponseRequest;
import org.wgx.payments.facade.Facade;
import org.wgx.payments.signature.AccountFactory;
import org.wgx.payments.utils.AccountUtils;
import org.wgx.payments.utils.AlipayConstants;
import org.wgx.payments.utils.AlipayUtils;
import org.wgx.payments.utils.RSAUtils;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

/**
 * Alipay processor's validator.
 *
 */
@Slf4j
public class AlipayValidator implements Validator<CreateOrUpdatePaymentResponseRequest> {

    @Setter
    @Resource(name = "alipayFacade")
    private Facade<Pair<String, String>, String> alipayFacade;

    @Setter
    @Resource(name = "accountFactory")
    private AccountFactory accountFactory;

    private static final String AGREEMENT_NO = "agreement_no";
    private static final String SIGN = "sign";
    private static final String SIGN_TYPE = "sign_type";
    private static final String NOTIFY_ID = "notify_id";
    private static final String OUT_TRADE_NO = "out_trade_no";
    private static final String TRADE_STATUS = "trade_status";
    private static final String BATCH_NO = "batch_no";
    private static final String RESULT_DETAILS = "result_details";
    private static final String TRADE_NO = "trade_no";
    private static final String ALIPAY_USER_ID = "alipay_user_id";

    private static final ImmutableList<String> CHARGE_BASIC_KEYS = ImmutableList.<String>builder()
            .add(OUT_TRADE_NO)
            .add(SIGN)
            .add(SIGN_TYPE)
            .add(TRADE_NO)
            .add(TRADE_STATUS)
            .build();

    private static final ImmutableList<String> REFUND_BASIC_KEYS = ImmutableList.<String>builder()
            .add(BATCH_NO)
            .add(SIGN)
            .add(SIGN_TYPE)
            .add(RESULT_DETAILS)
            .build();

    private static final ImmutableList<String> SIGN_BASIC_KEYS = ImmutableList.<String>builder()
            .add(AGREEMENT_NO)
            .add(SIGN)
            .add(SIGN_TYPE)
            .add(ALIPAY_USER_ID)
            .build();

    private static final ImmutableList<String> RESCIND_BASIC_KEYS = ImmutableList.<String>builder()
            .add(AGREEMENT_NO)
            .add(SIGN)
            .add(SIGN_TYPE)
            .add(ALIPAY_USER_ID)
            .build();

    private static final ImmutableList<String> SCHEDULED_PAY_BASIC_KEYS = ImmutableList.<String>builder()
            .add(OUT_TRADE_NO)
            .add(SIGN)
            .add(SIGN_TYPE)
            .add(TRADE_NO)
            .build();

    private static final ImmutableMap<String, List<String>> BASIC_INFO_KEYS = ImmutableMap.<String, List<String>>builder()
            .put(PaymentOperation.CHARGE.operationType(), CHARGE_BASIC_KEYS)
            .put(PaymentOperation.REFUND.operationType(), REFUND_BASIC_KEYS)
            .put(PaymentOperation.SIGN.operationType(), SIGN_BASIC_KEYS)
            .put(PaymentOperation.RESCIND.operationType(), RESCIND_BASIC_KEYS)
            .put(PaymentOperation.SCHEDULEDPAY.operationType(), SCHEDULED_PAY_BASIC_KEYS)
            .build();

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean validate(final CreateOrUpdatePaymentResponseRequest request) {
        Map<String, String> parameters = prepareMap(request);
        if (!validateBasicInfo(request.getPaymentOperationType(), parameters) || !verify(parameters)) {
            return false;
        }
        return true;
    }

    private boolean verify(final Map<String, String> parameters) {
        String signature = parameters.get(SIGN);
        String content = buildContent(parameters);
        boolean succeed = false;
        for (String account : accountFactory.getAlipayAccountList()) {
            succeed = RSAUtils.verify(content, signature, accountFactory.getPublicKeyByMaterialName(accountFactory.getMaterialNameByAccountName(account)),
                    AlipayConstants.INPUT_CHARSET);
            if (succeed) {
                log.info("Account [{}] used to verify the incoming request", account);
                AccountUtils.set(accountFactory.getAccount(account));
                break;
            }
        }
        if (!succeed) {
            return false;
        }
        String notifyID = parameters.get(NOTIFY_ID);
        if (StringUtils.isNotBlank(notifyID)) {
            StringBuilder verifyURLBuilder = new StringBuilder();
            verifyURLBuilder.append(AlipayConstants.HTTPS_VERIFY_URL)
                            .append("partner=")
                            .append(AccountUtils.get().getAccountNo())
                            .append("&notify_id=")
                            .append(notifyID);
            Pair<String, String> pair = Pair.of(verifyURLBuilder.toString(), null);
            if (!"true".equals(alipayFacade.call(pair))) {
                return false;
            }
        }
        return true;
    }

    private boolean validateBasicInfo(final String operationType, final Map<String, String> parameters) {
        List<String> keys = BASIC_INFO_KEYS.get(operationType);
        for (String key : keys) {
            if (parameters.get(key) == null || parameters.get(key).length() == 0) {
                return false;
            }
            if (key.equals(SIGN_TYPE) && !AlipayConstants.SIGN_TYPE.equals(parameters.get(key))) {
                return false;
            }
        }
        return true;
    }

    private Map<String, String> prepareMap(final CreateOrUpdatePaymentResponseRequest request) {
        Map<String, String> parameters = new HashMap<>();
        Map<String, String[]> requestParams = request.getParameters();
        for (Map.Entry<String, String[]> entry : requestParams.entrySet()) {
            String[] values = entry.getValue();
            StringBuilder valueStr = new StringBuilder(1024);
            for (int i = 0; i < values.length; i++) {
                if (i == values.length - 1) {
                    valueStr.append(values[i]);
                } else {
                    valueStr.append(values[i] + ",");
                }
            }
            parameters.put(entry.getKey(), valueStr.toString());
        }
        return parameters;
    }

    private String buildContent(final Map<String, String> parameters) {
        SortedMap<String, String> sortedMap = new TreeMap<>();
        Set<String> keys = parameters.keySet();
        keys.stream().forEach(key -> {
            String value = parameters.get(key);
            if (!(value == null || value.equals("") || key.equalsIgnoreCase(SIGN) || key.equalsIgnoreCase(SIGN_TYPE))) {
                sortedMap.put(key, value);
            }
        });
        return AlipayUtils.createLinkString(sortedMap, false);
    }
}
