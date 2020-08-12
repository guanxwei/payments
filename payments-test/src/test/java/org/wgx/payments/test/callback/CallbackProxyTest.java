package org.wgx.payments.test.callback;

import static org.testng.Assert.assertEquals;

import java.util.HashMap;
import java.util.Map;

import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.stream.extension.clients.RedisClient;
import org.testng.Assert;
import org.testng.annotations.Test;
import org.wgx.payments.callback.CallbackEvent;
import org.wgx.payments.callback.CallbackProxy;
import org.wgx.payments.callback.CallbackStatus;
import org.wgx.payments.client.api.io.CallbackMetaInfo;
import org.wgx.payments.model.FastSearchTableItem;
import org.wgx.payments.tools.Jackson;

public class CallbackProxyTest {

    @InjectMocks
    private CallbackProxy proxy;

    @Mock
    private RedisClient redisService;

    @org.testng.annotations.BeforeMethod
    public void BeforeMethod() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void test() {
        CallbackMetaInfo callbackMetaInfo = new CallbackMetaInfo();
        Map<String, String> parameters = new HashMap<>();
        parameters.put("transactionID", "transactionID");
        callbackMetaInfo.setParameters(parameters);
        Mockito.when(redisService.lpush(Mockito.anyString(), Mockito.any())).thenReturn(1L);
        Mockito.when(redisService.set(Mockito.anyString(), Mockito.anyString())).thenReturn(true);

        proxy.call(callbackMetaInfo);

        ArgumentCaptor<String> valueCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> keyCaptor = ArgumentCaptor.forClass(String.class);

        Mockito.verify(redisService).set(keyCaptor.capture(), valueCaptor.capture());

        Assert.assertEquals(keyCaptor.getValue(), "transactionID_callback");
        CallbackEvent callbackEvent = Jackson.parse(valueCaptor.getValue(), CallbackEvent.class);
        assertEquals(callbackEvent.getTimes(), 0);
        FastSearchTableItem item = Jackson.parse(callbackEvent.getDetail(), FastSearchTableItem.class);
        Assert.assertEquals(item.getItemKey(), "callback_transactionID");
        Assert.assertEquals(item.getTransactionID(), "transactionID");
        Assert.assertEquals(item.getStatus(), CallbackStatus.PENDING.status());
    }
}
