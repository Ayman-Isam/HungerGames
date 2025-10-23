package me.aymanisam.hungergames.stats;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import me.aymanisam.hungergames.HungerGames;
import me.aymanisam.hungergames.handlers.LangHandler;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.entity.Player;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Base64;
import java.util.logging.Level;

public class DisplayStatsHandler {
	private final HungerGames plugin;
	private final LangHandler langHandler;

	public DisplayStatsHandler(HungerGames plugin, LangHandler langHandler) {
		this.plugin = plugin;
		this.langHandler = langHandler;
	}

	public void displayPlayerHead(Player player) {
		player.sendMessage(langHandler.getMessage(player, "stats.player", player.getName()));

		String uuid = getPlayerUUID(player.getName());
		if (uuid == null) {
			player.sendMessage("Could not fetch player UUID.");
			return;
		}

		String skinUrl = getPlayerSkinUrl(uuid);
		if (skinUrl == null) {
			player.sendMessage("Could not fetch player skin.");
			return;
		}

		BufferedImage playerHead = getPlayerHead(skinUrl);
		if (playerHead == null) {
			player.sendMessage("Could not load player head.");
			return;
		}

		Component headComponent = getHeadAsTextComponent(playerHead);
		plugin.adventure().player(player).sendMessage(headComponent);
	}

	private String getPlayerUUID(String username) {
		try {
			URL url = new URL("https://api.mojang.com/users/profiles/minecraft/" + username);
			HttpURLConnection connection = (HttpURLConnection) url.openConnection();
			connection.setRequestMethod("GET");
			connection.setRequestProperty("Accept", "application/json");

			InputStreamReader reader = new InputStreamReader(connection.getInputStream());
			JsonObject jsonObj = JsonParser.parseReader(reader).getAsJsonObject();
			reader.close();

			return jsonObj.get("id").getAsString();
		} catch (Exception e) {
			plugin.getLogger().log(Level.WARNING, "Failed to fetch player UUID: " + e.getMessage(), e);
			return null;
		}
	}

	private String getPlayerSkinUrl(String uuid) {
		try {
			URL url = new URL("https://sessionserver.mojang.com/session/minecraft/profile/" + uuid);
			HttpURLConnection connection = (HttpURLConnection) url.openConnection();
			connection.setRequestMethod("GET");
			connection.setRequestProperty("Accept", "application/json");

			JsonObject jsonObj = JsonParser.parseReader(new InputStreamReader(connection.getInputStream())).getAsJsonObject();
			JsonObject properties = jsonObj.getAsJsonArray("properties").get(0).getAsJsonObject();
			String base64Value = properties.get("value").getAsString();

			String decoded = new String(Base64.getDecoder().decode(base64Value));
			JsonObject textureJson = JsonParser.parseString(decoded).getAsJsonObject();

			return textureJson.getAsJsonObject("textures").getAsJsonObject("SKIN").get("url").getAsString();
		} catch (Exception e) {
			plugin.getLogger().log(Level.WARNING, "Failed to fetch player skin URL: " + e.getMessage(), e);
			return null;
		}
	}

	private BufferedImage getPlayerHead(String imageUrl) {
		try (InputStream in = new URL(imageUrl).openStream()) {
			BufferedImage skin = ImageIO.read(in);
			return skin.getSubimage(8,8, 8, 8);
		} catch (Exception e) {
			plugin.getLogger().log(Level.WARNING, "Failed to download player skin: " + e.getMessage(), e);
			return null;
		}
	}

	private Component getHeadAsTextComponent(BufferedImage headImage) {
		if (headImage == null) return Component.text("⚠ Could not load head");

		TextComponent.Builder textBuilder = Component.text().content("");

		textBuilder.append(Component.newline());

		for (int y = 0; y < headImage.getHeight(); y++) {
			for (int x = 0; x < headImage.getWidth(); x++) {
				int color = headImage.getRGB(x, y) & 0xFFFFFF;
				TextColor textColor = TextColor.color(color);
				textBuilder.append(Component.text("█").color(textColor));
			}
			textBuilder.append(Component.newline());
		}
		return textBuilder.build();
	}
}
