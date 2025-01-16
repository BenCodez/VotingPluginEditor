package com.bencodez.votingplugineditor.generator;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.bukkit.Material;

/**
 * Generates a materials.json file containing all Material values at build time.
 */
public class MaterialGenerator {
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
	}
}
