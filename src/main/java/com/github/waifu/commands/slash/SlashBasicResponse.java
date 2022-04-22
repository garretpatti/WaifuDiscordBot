package com.github.waifu.commands.slash;

import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.privileges.CommandPrivilege;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Map;

public class SlashBasicResponse extends SlashCommandHandler {

    private final Logger LOGGER = LoggerFactory.getLogger(SlashBasicResponse.class);

    private final String command;
    private final String response;
    private final String description;

    public SlashBasicResponse(String command, String response, String description) {
        this.command = command;
        this.response = response;
        this.description = description;
    }

    public String getName() { return this.command; }

    @Nonnull
    @Override
    public CommandData getCommand() {
        return new CommandData(this.getName(), this.description);
    }

    @Override
    public boolean isGlobal() { return false; }

    @Override
    public Map<Long, List<CommandPrivilege>> getPrivileges() {
        return Map.of(
            879891493840617543L, List.of()
        );
    }

    @Override
    public void onCommand(SlashCommandEvent event) {
        event.reply(this.response).queue();
    }
}
