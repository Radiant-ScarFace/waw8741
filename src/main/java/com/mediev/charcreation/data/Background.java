package com.mediev.charcreation.data;

public enum Background {
    FARMER("Farmer"),
    BLACKSMITH("Blacksmith"),
    MERCHANT("Merchant"),
    HUNTER("Hunter"),
    NOBLE("Noble"),
    PRIEST("Priest"),
    SCHOLAR("Scholar"),
    CARPENTER("Carpenter"),
    MINER("Miner"),
    FISHERMAN("Fisherman"),
    GUARD_RECRUIT("Guard Recruit"),
    TAILOR("Tailor"),
    STABLE_WORKER("Stable Worker");

    private final String displayName;

    Background(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}