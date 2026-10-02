package net.veroxuniverse.verox_rpg_prog.stage;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.neoforged.fml.ModList;

public final class ModRequirements {

    private static final String REQUIRED_MODS = "required_mods";
    private static final String EXCLUDED_MODS = "excluded_mods";

    private ModRequirements() {}

    public static boolean areMet(JsonElement json) {
        if (!json.isJsonObject()) return true;
        JsonObject object = json.getAsJsonObject();

        for (String modId : readList(object, REQUIRED_MODS)) {
            if (!ModList.get().isLoaded(modId)) return false;
        }
        for (String modId : readList(object, EXCLUDED_MODS)) {
            if (ModList.get().isLoaded(modId)) return false;
        }
        return true;
    }

    private static String[] readList(JsonObject object, String key) {
        if (!object.has(key) || !object.get(key).isJsonArray()) return new String[0];

        JsonArray array = object.getAsJsonArray(key);
        String[] result = new String[array.size()];
        for (int i = 0; i < array.size(); i++) {
            result[i] = array.get(i).getAsString();
        }
        return result;
    }
}