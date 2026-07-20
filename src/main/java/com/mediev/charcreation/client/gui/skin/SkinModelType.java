package com.mediev.charcreation.client.gui.skin;

/**
 * The two vanilla player model arm layouts.
 * CLASSIC = "Steve" (4px wide arms), SLIM = "Alex" (3px wide arms).
 *
 * A skin file's model type is inferred from its file name suffix:
 *   somefile_slim.png    -> SLIM
 *   somefile.png         -> CLASSIC (default)
 *
 * This keeps skin authoring hardcode-free: dropping a new PNG in with the
 * right suffix is enough, no code or GUI changes required.
 */
public enum SkinModelType {
    CLASSIC,
    SLIM;

    private static final String SLIM_SUFFIX = "_slim";

    public static SkinModelType fromFileName(String fileNameWithoutExtension) {
        return fileNameWithoutExtension.toLowerCase().endsWith(SLIM_SUFFIX) ? SLIM : CLASSIC;
    }

    /**
     * Strips the model-type suffix (if any) so it isn't shown in the display name.
     */
    public static String stripSuffix(String fileNameWithoutExtension) {
        if (fileNameWithoutExtension.toLowerCase().endsWith(SLIM_SUFFIX)) {
            return fileNameWithoutExtension.substring(0, fileNameWithoutExtension.length() - SLIM_SUFFIX.length());
        }
        return fileNameWithoutExtension;
    }
}