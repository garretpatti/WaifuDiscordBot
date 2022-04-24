package com.github.waifu.commands.slash;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.privileges.CommandPrivilege;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class SlashMagic8 extends SlashCommandHandler {

    private static final List<String> answers = new ArrayList<>();

    public SlashMagic8() {
        answers.addAll(
            List.of(
                "Yes",
                "It is certain",
                "Without a doubt",
                "For sure",
                "You may rely on it",
                "No",
                "Absolutely not",
                "Probably not",
                "Don't count on it",
                "Outlook doesn't look so hot",
                "My sources say no",
                "The answer isn't not yes",
                "The answer isn't not no",
                "If your name isn't Alex, then yes",
                "Go ask magic7",
                "What does magic7 say?",
                "Better not tell you now",
                "Very doubtful",
                "Fat chance",
                "Ask again later",
                "...",
                "If your name is Bruce, then no"
            )
        );
    }

    @Override
    public String getName() {
        return "magic8";
    }

    @NotNull
    @Override
    public CommandData getCommand() {
        return Commands.slash(this.getName(), "Peer into the depths of your destiny.")
                .addOption(OptionType.STRING, "inquiry", "What do you want to ask", true);
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
    public void onCommand(SlashCommandInteractionEvent event) {
        event.reply(answers.get((int)(Math.random() * answers.size()))).queue();
    }
}
