package com.github.waifu;

import com.github.waifu.chat.ChatCenter;
import com.github.waifu.chat.ResponseCenter;
import com.github.waifu.interactions.InteractionCenter;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.GatewayIntent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

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

	public static App getSingleton() { return singleton; }

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
		InteractionCenter cmdCntr = InteractionCenter.getSingleton();
		ResponseCenter rspCntr = ResponseCenter.getSingleton();
		ChatCenter chtCntr = ChatCenter.getInstance();
		builder.addEventListeners(singleton, rspCntr, cmdCntr, chtCntr);
		JDA bot;
		try {
			bot = builder.build();
		}
		catch (Exception e) {
			LOGGER.error("An invalid token was provided in secrets.json: " + TOKEN, e);
			return;
		}

		bot.awaitReady();
		cmdCntr.registerCommands(bot);
		ScheduledExecutorService service = Executors.newSingleThreadScheduledExecutor();
		Runnable runnable = () -> {
			try {
				ChatCenter.saveMappings();
			}
			catch (IOException ioe) {
				LOGGER.error("An error occurred while trying to save the rfr mappings", ioe);
			}
		};
		ScheduledFuture<?> future = service.scheduleAtFixedRate(runnable, 1, 10, TimeUnit.MINUTES);
		new Thread(() -> {
			Scanner input = new Scanner(System.in);
			while (true) {
				if (input.hasNext()) {
					String cmd = input.next();
					if (cmd.equals("stop")) {
						bot.shutdown();
						LOGGER.info("Bot shutting down.");
						break;
					}
					else if (cmd.equals("kill")) {
						bot.shutdownNow();
						LOGGER.warn("Bot is being forcefully shut down.");
						break;
					}
				}
			}
			input.close();
			try {
				service.shutdown();
				future.cancel(false);
				ChatCenter.saveMappings();
			}
			catch (IOException e) {
				e.printStackTrace();
			}
		}, "App-Shutdown-Hook").start();
	}
}
