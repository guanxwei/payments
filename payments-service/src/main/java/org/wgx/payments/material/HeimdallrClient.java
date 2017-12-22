package org.wgx.payments.material;

import java.net.InetAddress;
import java.net.UnknownHostException;

import javax.annotation.Resource;

import org.wgx.payments.material.io.CreateMaterialRequest;
import org.wgx.payments.material.io.CreateMaterialResponse;
import org.wgx.payments.material.io.RetrieveMaterialRequest;
import org.wgx.payments.material.io.RetrieveMaterialResponse;

/**
 * Heimdallr service's client provided for upstream clients, upstream clients can use this entity to communicate with
 * Heimdallr service. 
 *
 *
 */
public class HeimdallrClient {

    @Resource(name = "heimdallrCreateMaterialService")
    private HeimdallrService<CreateMaterialRequest, CreateMaterialResponse> heimdallrCreateMaterialService;

    @Resource(name = "heimdallrRetrieveMaterialService")
    private HeimdallrService<RetrieveMaterialRequest, RetrieveMaterialResponse> heimdallrRetrieveMaterialService;

    /**
     * Call heimdallr service to create new material.
     * @param request Create material request.
     * @return Create material response.
     */
    public CreateMaterialResponse create(final CreateMaterialRequest request) {
        return heimdallrCreateMaterialService.execute(request);
    }

    /**
     * Call heimdallr service to retrieve material.
     * @param request Retrieve material request.
     * @return Retrieve material response.
     * @throws UnknownHostException 
     */
    public RetrieveMaterialResponse retrieve(final String name) throws UnknownHostException {
        RetrieveMaterialRequest request = new RetrieveMaterialRequest();
        request.setName(name);
        String host = null;
        host = System.getenv("COMPUTERNAME");
        if (host == null) {
            host = InetAddress.getLocalHost().getHostName();
        }
        request.setHost(host);
        String hostClass = System.getProperty("application.host.class");
        request.setHostClass(hostClass);
        RetrieveMaterialResponse response = heimdallrRetrieveMaterialService.execute(request);
        if (response.getCode() != 200) {
            throw new RuntimeException(response.getMessage());
        }
        return response;
    }
}
