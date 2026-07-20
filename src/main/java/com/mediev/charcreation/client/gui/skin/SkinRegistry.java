package com.mediev.charcreation.client.gui.skin;

import com.mediev.charcreation.data.Gender;
import net.minecraft.client.MinecraftClient;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Dynamically discovers skins from resources instead of a hardcoded list.
 *
 * Any resource pack / mod jar can contribute skins simply by placing PNG files at:
 *   assets/<namespace>/skins/male/<file>.png
 *   assets/<namespace>/skins/female/<file>.png
 *
 * The GUI never needs to know about individual files: {@link #getSkins(Gender)}
 * always reflects exactly what's currently on disk / in the resource manager.
 *
 * Results are cached per resource-manager instance (i.e. per resource reload),
 * so normal gameplay doesn't re-scan every frame, but an in-game resource
 * reload (F3+T, or a resource pack change) is picked up automatically on the
 * next call because the ResourceManager instance itself changes on reload.
 */
public final class SkinRegistry {
    private static final String MALE_PATH = "skins/male";
    private static final String FEMALE_PATH = "skins/female";
    private static final String PNG_SUFFIX = ".png";

    private static volatile ResourceManager cachedManager;
    private static volatile List<SkinEntry> cachedMale = Collections.emptyList();
    private static volatile List<SkinEntry> cachedFemale = Collections.emptyList();

    private SkinRegistry() {
    }

    public static List<SkinEntry> getSkins(Gender gender) {
        ensureFresh();
        return gender == Gender.MALE ? cachedMale : cachedFemale;
    }

    /**
     * Forces a re-scan on the next access, e.g. after a manual resource reload
     * triggered while the character creator is open.
     */
    public static void invalidate() {
        cachedManager = null;
    }

    private static synchronized void ensureFresh() {
        ResourceManager manager = MinecraftClient.getInstance().getResourceManager();
        if (manager == cachedManager) {
            return;
        }
        cachedManager = manager;
        cachedMale = scan(manager, MALE_PATH, Gender.MALE);
        cachedFemale = scan(manager, FEMALE_PATH, Gender.FEMALE);
    }

    private static List<SkinEntry> scan(ResourceManager manager, String path, Gender gender) {
        List<SkinEntry> result = new ArrayList<>();
        Map<Identifier, ?> found = manager.findResources(path, id -> id.getPath().endsWith(PNG_SUFFIX));
        for (Identifier identifier : found.keySet()) {
            String fullPath = identifier.getPath();
            int lastSlash = fullPath.lastIndexOf('/');
            String fileName = lastSlash >= 0 ? fullPath.substring(lastSlash + 1) : fullPath;
            String withoutExtension = fileName.substring(0, fileName.length() - PNG_SUFFIX.length());

            SkinModelType modelType = SkinModelType.fromFileName(withoutExtension);
            String displayName = toDisplayName(SkinModelType.stripSuffix(withoutExtension));
            String id = identifier.getNamespace() + ":" + withoutExtension;

            result.add(new SkinEntry(id, gender, identifier, modelType, displayName));
        }
        result.sort((a, b) -> a.displayName().compareToIgnoreCase(b.displayName()));
        return List.copyOf(result);
    }

    private static String toDisplayName(String rawFileName) {
        String[] parts = rawFileName.replace('-', '_').split("_");
        StringBuilder builder = new StringBuilder();
        for (String part : parts) {
            if (part.isEmpty()) {
                continue;
            }
            if (builder.length() > 0) {
                builder.append(' ');
            }
            builder.append(Character.toUpperCase(part.charAt(0)))
                    .append(part.substring(1).toLowerCase(Locale.ROOT));
        }
        return builder.length() > 0 ? builder.toString() : rawFileName;
    }
}