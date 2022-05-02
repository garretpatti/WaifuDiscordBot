package com.github.waifu.interactions.slash;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public interface ISlashInteraction {
    @Nonnull
    String getName();

    @Nonnull
    CommandData getCommand();

    @Nullable
    default List<Long> getGuilds() { return List.of(); }

    void onCommand(SlashCommandInteractionEvent event);
}
