package com.github.waifu;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.privileges.CommandPrivilege;
import net.dv8tion.jda.api.requests.restaction.CommandListUpdateAction;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.HttpURLConnection;
import java.net.URL;

import static net.dv8tion.jda.api.interactions.commands.privileges.CommandPrivilege.Type.ROLE;

public class CommandCenter extends ListenerAdapter {

    public static Logger LOGGER = LoggerFactory.getLogger(CommandCenter.class);
    private static final CommandCenter singleton = new CommandCenter();

    private CommandCenter() {}

    public static CommandCenter getSingleton() { return singleton; }

    // should be called after bot starts up
    public void registerCommands(@NotNull JDA bot) {
        LOGGER.info("Registering slash commands for WDO");
        Guild server = bot.getGuildById(879891493840617543L);
        CommandListUpdateAction commands = server.updateCommands();
        commands.addCommands(
            new CommandData("nh", "Get the quicklink for an nh extension.")
                .addOptions(
                    new OptionData(OptionType.INTEGER, "extension", "The extension to get")
                    .setRequiredRange(0, 999999).setRequired(true)
                ).setDefaultEnabled(false)
        ).queue(l -> {
            Command nh = l.get(0);
            server.updateCommandPrivilegesById(nh.getId(), new CommandPrivilege(ROLE, true, 880713006181404692L)).queue();
        });
        LOGGER.info("Slash commands finished registering.");
    }

    @Override
    public void onSlashCommand(@NotNull SlashCommandEvent event) {
        LOGGER.debug("Capturing slash event for command " + event.getName());
        if (event.isFromGuild()) {
            switch (event.getName()) {
                case "nh" -> nh(event);
                default -> {}
            }
        }
    }

    private void nh(@NotNull SlashCommandEvent event) {
        LOGGER.debug("Replying to nh command.");
        TextChannel ch = (TextChannel) event.getChannel();
        if (ch.isNSFW()) {
            event.deferReply().queue();
            String ext = String.format("%06d", event.getOption("extension").getAsLong());
            LOGGER.debug("Getting link for extension " + ext);
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
                        event.getHook().sendMessage("Sorry Senpai, I couldn't find that one.").queue();
                    } else {
                        event.getHook().sendMessage(url).queue();
                    }
                } catch (Exception e) {
                    event.getHook().sendMessage("Sorry Senpai, something happened while trying to find that.").queue();
                    LOGGER.warn("An error occurred while retrieving the page from nhentai.", e);
                } finally {
                    if (connection != null) {
                        connection.disconnect();
                    }
                }
            }).start();
        }
        else {
            event.reply("Sorry Senpai. I can't share that in this channel.").queue();
        }
    }
}
