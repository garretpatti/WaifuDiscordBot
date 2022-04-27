package com.github.waifu.commands.slash;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.privileges.CommandPrivilege;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;

public interface ISlashHandler {
    @Nonnull
    String getName();

    @Nonnull
    CommandData getCommand();

    default boolean isGlobal() { return true; }

    @Nullable
    Map<Long, List<CommandPrivilege>> getPrivileges();

    void onCommand(SlashCommandInteractionEvent event);
}
