package com.github.waifu.commands.slash;

import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.privileges.CommandPrivilege;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Map;

import static net.dv8tion.jda.api.interactions.commands.privileges.CommandPrivilege.Type.ROLE;

public class SlashNh extends SlashCommandHandler {

    private final Logger LOGGER = LoggerFactory.getLogger(SlashNh.class);

    public String getName() { return "nh"; }

    @Nonnull
    @Override
    public CommandData getCommand() {
        return Commands.slash(this.getName(), "Get the quicklink for an nh extension.")
            .setDefaultEnabled(false)
            .addOptions(
                new OptionData(OptionType.INTEGER, "extension", "The extension to get")
                    .setRequiredRange(0, 999999).setRequired(true)
            );
    }

    @Override
    public boolean isGlobal() { return false; }

    @Nullable
    @Override
    public Map<Long, List<CommandPrivilege>> getPrivileges() {
        return Map.of(
            879891493840617543L, List.of(
                    new CommandPrivilege(ROLE, true, 880713006181404692L)
                )
        );
    }

    @Override
    public void onCommand(SlashCommandInteractionEvent event) {
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
