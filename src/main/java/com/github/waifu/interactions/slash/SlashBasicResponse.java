package com.github.waifu.interactions.slash;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;

import javax.annotation.Nonnull;

public class SlashBasicResponse implements ISlashInteraction {

    @SlashCommand
    public static final SlashBasicResponse ping = new SlashBasicResponse("ping", "Pong!", "Ping test");
    @SlashCommand
    public static final SlashBasicResponse bing = new SlashBasicResponse("bing", "Bong!", "Bing bong!");
    @SlashCommand
    public static final SlashBasicResponse oauth2 = new SlashBasicResponse("oauth2",
        "https://discord.com/api/oauth2/authorize?client_id=933960413534617611&permissions=1574075624529&scope=bot%20applications.commands",
        "Get the invite link for this bot");

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
    public void onCommand(@Nonnull SlashCommandInteractionEvent event) {
        event.reply(this.response).queue();
    }
}
