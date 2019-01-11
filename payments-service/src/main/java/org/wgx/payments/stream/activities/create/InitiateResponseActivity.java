package org.wgx.payments.stream.activities.create;

import org.springframework.stereotype.Component;
import org.stream.core.component.Activity;
import org.stream.core.component.ActivityResult;
import org.stream.core.execution.WorkFlowContext;
import org.stream.core.resource.Resource;
import org.wgx.payments.client.api.io.CreatePaymentResponse;

import lombok.extern.slf4j.Slf4j;

/**
 * Stream work-flow based activity to initiate create payment response.
 * @author weigu
 *
 */
@Slf4j
@Component
public class InitiateResponseActivity extends Activity {

    /**
     * {@inheritDoc}
     */
    @Override
    public ActivityResult act() {
        CreatePaymentResponse createPaymentResponse = new CreatePaymentResponse();
        createPaymentResponse.setResponseCode(200);
        Resource resource = Resource.builder()
                .resourceReference(WorkFlowContext.WORK_FLOW_RESPONSE_REFERENCE)
                .value(createPaymentResponse)
                .build();
        WorkFlowContext.attachResource(resource);
        log.info("Create payment response initiated");
        return ActivityResult.SUCCESS;
    }

}
