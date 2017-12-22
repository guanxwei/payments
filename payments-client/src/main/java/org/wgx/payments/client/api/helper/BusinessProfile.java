package org.wgx.payments.client.api.helper;

/**
 * Business profiles recognized by Payments-Platform.
 *
 */
public enum BusinessProfile {

    /**
     * Retail business.
     */
    RETAIL("Retail", "零售商品", "01"),

    /**
     * Cloud-music VIP service business.
     */
    VIP("VIP", "普通会员", "02"),

    /**
     * Cloud-music super VIP service business.
     */
    SUPER_VIP("SuperVIP", "豪华会员", "03"),

    /**
     * Cloud-music auto VIP charge service business.
     */
    AUTO_VIP("AutoVIP", "会员自动续费", "04"),

    /**
     * Cloud storage business.
     */
    YUN_PAN("Yunpan1", "云音乐云盘", "05"),

    /**
     * Album purchase business.
     */
    ALBUM("Album", "专辑购买", "06"),

    /**
     * Single song purchase business.
     */
    SINGLE_SONG("SingleSong", "单曲购买", "07"),

    /**
     * Ticket purchase business.
     */
    TICKET("Ticket", "票务", "08"),

    /**
     * Radio subscribe business.
     */
    RADIO("Radio", "电台", "09"),

    /**
     * Any virtual goods purchase business excluding Album, single-song, yunpan and VIP.
     */
    VIRTUAL("Virtual", "其他类虚拟商品", "10"),

    /**
     * 3rd party goods.
     */
    THIRD_PARTY("ThirdParty", "第三方货品", "11"),

    /**
     * Currently unrecognized business profile.
     */
    UNRECOGNIZED("Unrecognized", "其他货品", "12");

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
        return UNRECOGNIZED;
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
        return UNRECOGNIZED;
    }
}
