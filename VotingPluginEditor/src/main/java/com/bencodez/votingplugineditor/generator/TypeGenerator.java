package com.bencodez.votingplugineditor.generator;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.bukkit.Material;
import org.bukkit.potion.PotionEffectType;

/**
 * Generates a materials.json file containing all Material values at build time.
 */
public class TypeGenerator {
	public static void main(String[] args) {
		try {
			// Output directory for the generated file (target/classes for Maven resource
			// directory)
			String outputPath = "src/main/resources/materials.json";
			Files.createDirectories(Paths.get("src/main/resources"));

			// Get all Material names
			List<String> materials = Arrays.stream(Material.values()).map(Material::name).collect(Collectors.toList());

			// Write them as a JSON array
			try (FileWriter writer = new FileWriter(outputPath)) {
				writer.write("[\n");
				for (int i = 0; i < materials.size(); i++) {
					writer.write("  \"" + materials.get(i) + "\"");
					if (i < materials.size() - 1)
						writer.write(",");
					writer.write("\n");
				}
				writer.write("]");
			}

			System.out.println("Generated materials.json at: " + outputPath);
		} catch (IOException e) {
			e.printStackTrace();
		}

		try {
			// Output directory for the generated file (target/classes for Maven resource
			// directory)
			String outputPath = "src/main/resources/potions.json";
			Files.createDirectories(Paths.get("src/main/resources"));

			// Get all PotionEffectType names

			List<String> potionTypes = Arrays.asList("ABSORPTION", "BAD_OMEN", "BLINDNESS", "CONDUIT_POWER",
					"CONFUSION", "DAMAGE_RESISTANCE", "DOLPHINS_GRACE", "FAST_DIGGING", "FIRE_RESISTANCE", "GLOWING",
					"HARM", "HEAL", "HEALTH_BOOST", "HERO_OF_THE_VILLAGE", "HUNGER", "INCREASE_DAMAGE", "INVISIBILITY",
					"JUMP", "LEVITATION", "LUCK", "NIGHT_VISION", "POISON", "REGENERATION", "SATURATION", "SLOW",
					"SLOW_DIGGING", "SLOW_FALLING", "SPEED", "UNLUCK", "WATER_BREATHING", "WEAKNESS", "WITHER");

			// Write them as a JSON array
			try (FileWriter writer = new FileWriter(outputPath)) {
				writer.write("[\n");
				for (int i = 0; i < potionTypes.size(); i++) {
					writer.write("  \"" + potionTypes.get(i) + "\"");
					if (i < potionTypes.size() - 1)
						writer.write(",");
					writer.write("\n");
				}
				writer.write("]");
			}

			System.out.println("Generated potions.json at: " + outputPath);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
