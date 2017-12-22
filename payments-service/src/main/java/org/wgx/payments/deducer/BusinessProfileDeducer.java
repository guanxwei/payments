package org.wgx.payments.deducer;

import org.wgx.payments.client.api.helper.BusinessProfile;

import com.google.common.collect.ImmutableMap;

/**
 * Business profile deducer to deduce business profile from the input string.
 *
 */
public class BusinessProfileDeducer implements Deducer<String, BusinessProfile> {

    private static final ImmutableMap<String, BusinessProfile> PROFILES = ImmutableMap.<String, BusinessProfile>builder()
            .put(BusinessProfile.ALBUM.profile(), BusinessProfile.ALBUM)
            .put(BusinessProfile.AUTO_VIP.profile(), BusinessProfile.AUTO_VIP)
            .put(BusinessProfile.RADIO.profile(), BusinessProfile.RADIO)
            .put(BusinessProfile.RETAIL.profile(), BusinessProfile.RETAIL)
            .put(BusinessProfile.SINGLE_SONG.profile(), BusinessProfile.SINGLE_SONG)
            .put(BusinessProfile.SUPER_VIP.profile(), BusinessProfile.SUPER_VIP)
            .put(BusinessProfile.THIRD_PARTY.profile(), BusinessProfile.THIRD_PARTY)
            .put(BusinessProfile.TICKET.profile(), BusinessProfile.TICKET)
            .put(BusinessProfile.VIP.profile(), BusinessProfile.VIP)
            .build();

    /**
     * {@inheritDoc}
     */
    @Override
    public BusinessProfile deduce(final String input) {
        if (!PROFILES.containsKey(input)) {
            return BusinessProfile.UNRECOGNIZED;
        } else {
            return PROFILES.get(input);
        }
    }

}
