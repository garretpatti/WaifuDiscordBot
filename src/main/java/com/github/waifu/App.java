package com.github.waifu;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.message.guild.react.GuildMessageReactionAddEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.GatewayIntent;

import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Spitfyre03
 */
public class App extends ListenerAdapter {

	public static String TOKEN;

	// Guild, Message, Role
	public static final Map<Long, Map<Long, Long>> reactionMap = new HashMap<>();

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

		Map<Long, Long> msgRoleMap = new HashMap<>();
		msgRoleMap.put(942656923390590996L, 942646740845232148L);
		reactionMap.put(879891493840617543L, msgRoleMap);

		bot.awaitReady();
	}

	@Override
	public void onGuildMessageReactionAdd(@Nonnull GuildMessageReactionAddEvent event) {
		Map<Long, Long> msgRoleMap;
		Guild server = event.getGuild();
		msgRoleMap = reactionMap.get(server.getIdLong());
		if (msgRoleMap != null) {
			Long roleLong = msgRoleMap.get(event.getMessageIdLong());
			if (roleLong != null) {
				Role role = server.getRoleById(roleLong);
				if (role != null) {
					event.retrieveMember().queue(member -> server.addRoleToMember(member, role).queue());
				}
			}
		}
		// else no react-for-role maps set up for this server
	}
}
