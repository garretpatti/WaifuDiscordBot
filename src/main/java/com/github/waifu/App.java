package com.github.waifu;

import com.github.waifu.commands.TenorHandler;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.message.guild.react.GuildMessageReactionAddEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.GatewayIntent;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.io.FileReader;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Spitfyre03
 */
public class App extends ListenerAdapter {

	public static String TOKEN;
	public static final Logger LOGGER = LoggerFactory.getLogger(App.class);

	// Guild, Message, Role
	public static final Map<Long, Map<Long, Long>> reactionMap = new HashMap<>();

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
		builder.addEventListeners(new App());
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

	@Override
	public void onMessageReceived(@Nonnull MessageReceivedEvent event) {
		Message msg = event.getMessage();
		MessageChannel channel = event.getChannel();
		if (channel.getType().equals(ChannelType.TEXT)) {
			TextChannel textChannel = (TextChannel) channel;
			if (!msg.getContentRaw().equals("")) {
				String strMsg = msg.getContentRaw().trim();
				Consumer<JSONObject> memeResponseConsumer = r -> {
					String url = r.getJSONArray("results").getJSONObject(0).getJSONArray("media").getJSONObject(0).getJSONObject("tinygif").getString("url");
					textChannel.sendMessage(url).queue();
				};
				Consumer<Exception> memeErrorConsumer = e -> System.out.println("Error - " + e.getMessage());

				if (strMsg.equals("!ping")) {
					textChannel.sendMessage("Pong!").queue();
				}
				else if (strMsg.equals("!bing")) {
					textChannel.sendMessage("Bong!").queue();
				}
				else if (strMsg.contains("!cagemebro")) {
					TenorHandler.getSearchResults("nick cage", memeResponseConsumer, memeErrorConsumer);
				}
				else if (strMsg.contains("!smashing")) {
					TenorHandler.getSearchResults("nigel thornberry smashing", memeResponseConsumer, memeErrorConsumer);
				}
				else if (strMsg.startsWith("!tenor") && strMsg.length() > 7) {
					TenorHandler.getSearchResults(strMsg.substring(7), memeResponseConsumer, memeErrorConsumer);
				}
				else if (textChannel.isNSFW() && strMsg.startsWith("!nh") && strMsg.length() >= 10){
					Matcher matcher = Pattern.compile("\\d{6}").matcher(strMsg.substring(4, 10));
					if (matcher.find()) {
						String ext = matcher.group(0);
						new Thread(() -> {
							String url = String.format("https://nhentai.net/g/%1$s", ext);
							HttpURLConnection connection = null;
							try {
								connection = (HttpURLConnection) new URL(url).openConnection();
								// Get request
								connection.setDoInput(true);
								connection.setDoOutput(true);
								connection.setRequestMethod("GET");
								connection.setRequestProperty("Content-Type", "text/html");
								connection.setRequestProperty("Accept", "text/html");
								connection.setRequestProperty("Content-Type", "text/html; charset=UTF-8");

								// Handle failure
								int statusCode = connection.getResponseCode();
								if (statusCode != HttpURLConnection.HTTP_OK && statusCode != HttpURLConnection.HTTP_CREATED) {
									textChannel.sendMessage("Sorry Senpai, I couldn't find that one.").queue();
								}
								else {
									textChannel.sendMessage(url).queue();
								}
							}
							catch (Exception ignored) {}
							finally {
								if (connection != null) {
									connection.disconnect();
								}
							}
						}).start();
					}
				}
			}
		}
	}
}
