package com.github.waifu.commands.slash;

import com.github.waifu.commands.slash.SlashCommandHandler;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.privileges.CommandPrivilege;
import net.dv8tion.jda.api.interactions.commands.privileges.CommandPrivilege.Type;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;

public class SlashAlex extends SlashCommandHandler {

    @Override
    public String getName() {
        return "alex";
    }

    @NotNull
    @Override
    public CommandData getCommand() {
        return new CommandData(this.getName(), "Hey Alex!");
    }

    @Override
    public boolean isGlobal() { return false; }

    @Override
    public Map<Long, List<CommandPrivilege>> getPrivileges() {
        return Map.of(
        879891493840617543L, List.of(
                new CommandPrivilege(Type.ROLE, true, 879895881896325150L)
            )
        );
    }

    @Override
    public void onCommand(SlashCommandEvent event) {
        event.deferReply().queue();
        event.getGuild().retrieveMemberById(312015250096521216L).queue( m -> {
            if (m != null) event.getHook().sendMessage(m.getAsMention()).queue();
            else event.getHook().sendMessage("We couldn't find Alex!").queue();
        });
    }
}
