package com.mediev.charcreation.data;

public enum Nationality {
    ELDRAVIA("Eldravia"),
    THORNGARD("Thorngard"),
    VALEMOOR("Valemoor"),
    ASHENFELL("Ashenfell"),
    KORINTHAL("Korinthal"),
    DUNMORE("Dunmore"),
    SILVERHOLD("Silverhold"),
    BLACKMARSH("Blackmarsh"),
    HIGHVALE("Highvale"),
    WINTERHOLT("Winterholt");

    private final String displayName;

    Nationality(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}