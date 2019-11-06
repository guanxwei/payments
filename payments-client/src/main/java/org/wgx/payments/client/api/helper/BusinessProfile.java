package org.wgx.payments.client.api.helper;

/**
 * Business profiles recognized by Payments-Platform.
 *
 */
public enum BusinessProfile {

    /**
     * Retail.
     */
    RETAIL("Retail", "零售", "001");

    private String profile;

    private String displayName;

    private String code;

    /**
     * Default constructor.
     * @param profile Business profile.
     * @param displayName Name that will be displayed in 3P payment gateway system.
     * @param code Business code.
     */
    BusinessProfile(final String profile, final String displayName, final String code) {
        this.profile = profile;
        this.displayName = displayName;
        this.code = code;
    }

    /**
     * Return this business profiles's name.
     * @return profile
     */
    public String profile() {
        return this.profile;
    }

    /**
     * Return this business profiles's showName.
     * @return profile
     */
    public String displayName() {
        return this.displayName;
    }

    /**
     * Return this business profile's code.
     * @return code.
     */
    public String code() {
        return this.code;
    }

    /**
     * Deduce business profile from the input string profile.
     * @param profile String profile.
     * @return BusinessProfile instance.
     */
    public static BusinessProfile fromProfile(final String profile) {
        for (BusinessProfile businessProfile : BusinessProfile.values()) {
            if (businessProfile.profile.equals(profile)) {
                return businessProfile;
            }
        }
        return RETAIL;
    }

    /**
     * Deduce business profile from the input show name.
     * @param showName Profile's show name.
     * @return Business profile instance.
     */
    public static BusinessProfile fromShowName(final String showName) {
        for (BusinessProfile businessProfile : BusinessProfile.values()) {
            if (businessProfile.displayName().equals(showName)) {
                return businessProfile;
            }
        }
        return RETAIL;
    }
}
