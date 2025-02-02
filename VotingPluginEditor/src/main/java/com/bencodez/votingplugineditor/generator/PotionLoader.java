
package com.bencodez.votingplugineditor.generator;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

public class PotionLoader {
	public static List<String> loadPotions() {
		List<String> potions = new ArrayList<>();
		try (InputStream inputStream = PotionLoader.class.getResourceAsStream("/potions.json")) {
			if (inputStream == null)
				throw new IllegalArgumentException("Potions file not found!");

			JsonArray jsonArray = JsonParser.parseReader(new InputStreamReader(inputStream)).getAsJsonArray();
			for (JsonElement element : jsonArray) {
				potions.add(element.getAsString());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return potions;
	}
}
