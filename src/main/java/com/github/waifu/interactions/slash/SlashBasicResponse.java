package com.github.waifu.interactions.slash;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.privileges.CommandPrivilege;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Map;

public class SlashBasicResponse implements ISlashInteraction {

    private final String command;
    private final String response;
    private final String description;

    public SlashBasicResponse(String command, String response, String description) {
        this.command = command;
        this.response = response;
        this.description = description;
    }

    @Nonnull
    @Override
    public String getName() { return this.command; }

    @Nonnull
    @Override
    public CommandData getCommand() {
        return Commands.slash(this.getName(), this.description);
    }

    @Override
    public List<Long> getGuilds() { return List.of(879891493840617543L); }

    @Override
    public void onCommand(SlashCommandInteractionEvent event) {
        event.reply(this.response).queue();
    }
}
