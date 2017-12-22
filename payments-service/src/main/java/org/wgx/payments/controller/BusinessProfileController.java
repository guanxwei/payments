package org.wgx.payments.controller;

import java.util.LinkedList;
import java.util.List;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.wgx.payments.client.api.helper.BusinessProfile;
import org.wgx.payments.tools.JsonObject;

/**
 * Business profile controller to manage business profile for backfill system.
 *
 */
@RestController
public class BusinessProfileController {

    /**
     * Get business profile.
     * @return Jsonfied business profile list.
     */
    @RequestMapping(path = "/api/music/payments/backfill/business/list")
    public JsonObject getBusinessProfileList() {
        List<String> businessProfiles = new LinkedList<>();
        for (BusinessProfile profile : BusinessProfile.values()) {
            businessProfiles.add(profile.profile());
        }
        return JsonObject.start().code(200).data(businessProfiles);
    }
}
