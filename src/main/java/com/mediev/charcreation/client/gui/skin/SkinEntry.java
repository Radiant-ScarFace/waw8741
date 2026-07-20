package com.mediev.charcreation.client.gui.skin;

import com.mediev.charcreation.data.Gender;
import net.minecraft.util.Identifier;

/**
 * One discovered skin. Instances are produced only by {@link SkinRegistry},
 * which discovers them from resources at runtime - nothing here is ever
 * hand-authored or hardcoded.
 *
 * @param id          stable identifier sent over the network / stored in save data,
 *                    e.g. "charcreation:male/knight_default"
 * @param gender      which gender bucket this skin belongs to
 * @param texture     the resource location of the skin PNG, ready to bind for rendering
 * @param modelType   classic (Steve) or slim (Alex) arm layout
 * @param displayName human-readable name shown in the GUI, derived from the file name
 */
public record SkinEntry(String id, Gender gender, Identifier texture, SkinModelType modelType, String displayName) {

    public boolean isSlim() {
        return modelType == SkinModelType.SLIM;
    }
}