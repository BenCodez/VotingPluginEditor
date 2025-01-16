package com.bencodez.votingplugineditor.generator;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

public class MaterialLoader {
    public static List<String> loadMaterials() {
        List<String> materials = new ArrayList<>();
        try (InputStream inputStream = MaterialLoader.class.getResourceAsStream("/materials.json")) {
            if (inputStream == null) throw new IllegalArgumentException("Materials file not found!");

            JsonArray jsonArray = JsonParser.parseReader(new InputStreamReader(inputStream)).getAsJsonArray();
            for (JsonElement element : jsonArray) {
                materials.add(element.getAsString());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return materials;
    }
}
