package com.github.waifu.commands.slash;

import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.privileges.CommandPrivilege;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;

public abstract class SlashCommandHandler {

    public abstract String getName();

    @Nonnull
    public abstract CommandData getCommand();

    public boolean isGlobal() { return true; }

    @Nullable
    public Map<Long, List<CommandPrivilege>> getPrivileges() { return Map.of(); }

    public abstract void onCommand(SlashCommandEvent event);
}
