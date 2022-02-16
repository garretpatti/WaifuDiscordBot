package com.github.waifu;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.GatewayIntent;

import java.io.FileReader;
import java.io.IOException;

/**
 * @author Spitfyre03
 */
public class App extends ListenerAdapter {

	public static String TOKEN;

	public static void main(String[] args) throws InterruptedException {
		try {
			String path = App.class.getResource("/secrets.json").getPath();
			JsonObject secretsTree = JsonParser.parseReader(new FileReader(path)).getAsJsonObject();
			TOKEN = secretsTree.get("jda-key").getAsString();
			if (TOKEN.equals("")) { throw new IllegalStateException("Token jda-key value is empty"); }
		}
		catch (IOException ioe) {
			ioe.printStackTrace();
			return;
		}
		catch (IllegalStateException | NullPointerException e) {
			System.out.println("The secrets file must contain an entry for jda-key");
			e.printStackTrace();
			return;
		}

		JDABuilder builder = JDABuilder.createLight(TOKEN, GatewayIntent.GUILD_MEMBERS, GatewayIntent.GUILD_MESSAGES, GatewayIntent.GUILD_MESSAGE_REACTIONS);
		builder.addEventListeners(new App());
		JDA bot;
		try {
			bot = builder.build();
		}
		catch (Exception e) {
			System.out.println("An invalid token was provided in secrets.json. Please remedy before running this bot");
			e.printStackTrace();
			return;
		}

		bot.awaitReady();
	}
}
