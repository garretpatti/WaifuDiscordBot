package com.github.waifu.commands;

import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandGroupData;
import net.dv8tion.jda.api.interactions.commands.privileges.CommandPrivilege;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class SlashPokemon extends SlashCommandHandler {

    public static final Logger LOGGER = LoggerFactory.getLogger(SlashPokemon.class);

    public String getName() { return "pokemon"; }

    @Nonnull
    public CommandData getCommand() {
        return new CommandData(this.getName(), "Who's that Pokemon?")
            .addSubcommands(
                new SubcommandData("random", "Draw a random Pokemon"),
                new SubcommandData("number", "Get a specific Pokemon by number")
                    .addOptions(
                        new OptionData(OptionType.INTEGER, "number", "The Pokemon's number").setRequiredRange(1, 898).setRequired(true)
                    ),
                new SubcommandData("name", "Get a specific Pokemon by name")
                    .addOptions(
                        new OptionData(OptionType.STRING, "name", "The Pokemon's name").setRequired(true)
                    )
            );
    }

    public boolean isGlobal() { return false; }

    @Nullable
    public Map<Long, List<CommandPrivilege>> getPrivileges() {
        return Map.of(
            879891493840617543L, List.of()
        );
    }

    public void onCommand(SlashCommandEvent event) {
        event.reply("This command is not yet supported. Please try again later.").setEphemeral(true).queue();
    }
}
