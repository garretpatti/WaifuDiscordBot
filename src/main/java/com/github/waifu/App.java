package com.github.waifu;

import com.github.waifu.chat.ResponseCenter;
import com.github.waifu.commands.slash.CommandCenter;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.GatewayIntent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Spitfyre03
 */
public class App extends ListenerAdapter {

	public static String TOKEN;
	public static final Logger LOGGER = LoggerFactory.getLogger(App.class);
	private static final App singleton = new App();

	// Guild, Message, Role
	public static final Map<Long, Map<Long, Long>> reactionMap = new HashMap<>();

	private App() {}

	public App getSingleton() { return singleton; }

	public static void main(String[] args) throws InterruptedException {
		try {
			LOGGER.info("Loading JDA Application token.");
			String path = App.class.getResource("/secrets.json").getPath();
			JsonObject secretsTree = JsonParser.parseReader(new FileReader(path)).getAsJsonObject();
			TOKEN = secretsTree.get("bot_token").getAsString();
			if (TOKEN.equals("")) { throw new IllegalStateException("Token bot_token value is empty"); }
			LOGGER.info("JDA Bot token retrieved. Logging in now.");
		}
		catch (IOException ioe) {
			LOGGER.error("There was an error while reading the token file.", ioe);
			return;
		}
		catch (IllegalStateException | NullPointerException e) {
			LOGGER.error("The secrets file must contain an entry for bot_token", e);
			return;
		}

		JDABuilder builder = JDABuilder.createLight(TOKEN, GatewayIntent.GUILD_MEMBERS, GatewayIntent.GUILD_MESSAGES, GatewayIntent.GUILD_MESSAGE_REACTIONS);
		CommandCenter cmdCntr = CommandCenter.getSingleton();
		ResponseCenter rspCntr = ResponseCenter.getSingleton();
		builder.addEventListeners(singleton, rspCntr, cmdCntr);
		JDA bot;
		try {
			bot = builder.build();
		}
		catch (Exception e) {
			LOGGER.error("An invalid token was provided in secrets.json: " + TOKEN, e);
			return;
		}

		Map<Long, Long> msgRoleMap = new HashMap<>();
		msgRoleMap.put(945152664059121685L, 942646740845232148L);
		msgRoleMap.put(945153619135709297L, 933660702294552586L);
		msgRoleMap.put(945154695587041331L, 880713006181404692L);
		reactionMap.put(879891493840617543L, msgRoleMap);

		bot.awaitReady();
		cmdCntr.registerCommands(bot);
	}

	@Override
	public void onMessageReactionAdd(@Nonnull MessageReactionAddEvent event) {
		if (event.isFromGuild()) {
			Guild server = event.getGuild();
			Map<Long, Long> msgRoleMap;
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
}
