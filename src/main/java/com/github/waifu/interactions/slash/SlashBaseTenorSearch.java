package com.github.waifu.interactions.slash;

import com.github.waifu.http.helpers.TenorHandler;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.function.Consumer;

public class SlashBaseTenorSearch implements ISlashInteraction {

    public static final Logger LOGGER = LoggerFactory.getLogger(SlashBaseTenorSearch.class);

    private final String name;
    private final String description;
    private final String searchPhrase;

    public SlashBaseTenorSearch(String cmd, String desc, String phrase) {
        this.name = cmd;
        this.description = desc;
        this.searchPhrase = phrase;
    }

    @Nonnull
    @Override
    public String getName() { return this.name; }

    @Nonnull
    @Override
    public CommandData getCommand() { return Commands.slash(this.name, this.description); }

    @Override
    public List<Long> getGuilds() { return List.of(879891493840617543L); }

    @Override
    public void onCommand(SlashCommandInteractionEvent event) {
        event.deferReply().queue();
        Consumer<Exception> error = e -> {
                LOGGER.error("An error was thrown while retrieving a gif from Tenor.", e);
                event.getHook().sendMessage("Sorry I couldn't get this gif. Please let Spitfyre know.").queue();
            };
        Consumer<JSONObject> response = r -> {
                try {
                    String url = r.getJSONArray("results").getJSONObject(0).getJSONArray("media").getJSONObject(0).getJSONObject("tinygif").getString("url");
                    event.getHook().sendMessage(url).queue();
                }
                catch (Exception e) {
                    error.accept(e);
                }
            };
        TenorHandler.getSearchResults(this.searchPhrase, response, error);
    }
}
