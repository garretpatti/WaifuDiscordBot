package com.github.waifu.commands.slash;

import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Icon;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import net.dv8tion.jda.api.interactions.commands.privileges.CommandPrivilege;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.List;
import java.util.Map;

public class SlashEmote extends SlashCommandHandler implements IButtonInteraction {

    public static final Logger LOGGER = LoggerFactory.getLogger(SlashEmote.class);

    @Override
    public String getName() { return "emote"; }

    @Nonnull
    @Override
    public CommandData getCommand() {
        return Commands.slash(this.getName(), "Emote manager")
                .addSubcommands(
                        new SubcommandData("add", "Add an emote")
                                .addOption(OptionType.STRING, "alias", "The name for the new emote", true)
                                .addOption(OptionType.ATTACHMENT, "emote", "The emote to add", true)
                );
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
        if (event.isFromGuild()) {
            event.deferReply().queue();
            String name = event.getOption("alias", "", OptionMapping::getAsString).toLowerCase();
            if (!name.equals("")) {
                event.getGuild().retrieveEmotes().queue(l -> {
                    if (l.stream().anyMatch(e -> e != null && e.getName().equalsIgnoreCase(name))) {
                        event.getHook().sendMessage("An emote already exists by this name. Please choose another alias.").queue();
                    } else {
                        event.getOption("emote", OptionMapping::getAsAttachment).downloadToFile().thenAcceptAsync(f -> {
                            if (event.getMember().hasPermission(Permission.MANAGE_EMOTES_AND_STICKERS)) {
                                try {
                                    event.getGuild().createEmote(name, Icon.from(f)).queue(e -> {
                                        event.getHook().editOriginal(new MessageBuilder()
                                                .setContent("Emote successfully created: " + e.getAsMention()).build()).queue();
                                    });
                                } catch (IOException ioe) {
                                    LOGGER.error("An error occurred while creating Icon for an emote file.", ioe);
                                }
                            } else {
                                event.getHook()
                                        .sendFile(f)
                                        .setContent(String.format("Pending moderator approval: %s", name))
                                        .addActionRow(this.getButtons()).queue();
                            }
                            f.delete();
                        });
                    }
                });
            }
        }
    }

    @Override
    public List<Button> getButtons() {
        return List.of(Button.success("emote-add-approve", "Approve"),
                Button.danger("emote-add-deny", "Deny"));
    }

    @Override
    public void onInteract(ButtonInteractionEvent event) {
        Guild guild = event.getGuild();
        if (guild != null) {
            Member member = event.getMember();
            if (member != null && member.hasPermission(Permission.MANAGE_EMOTES_AND_STICKERS)) {
                String id = event.getComponentId();
                if (id.equals("emote-add-approve")) {
                    event.deferEdit().queue();
                    String msg = event.getMessage().getContentDisplay();
                    String name = msg.substring(msg.indexOf(":") + 2);
                    event.getMessage().getAttachments().get(0).downloadToFile().thenAcceptAsync(f -> {
                        try {
                            event.getGuild().createEmote(name, Icon.from(f)).queue(e -> {
                                event.getHook().editOriginalComponents(List.of()).queue();
                                event.getHook().editOriginal(new MessageBuilder()
                                        .setContent("Request was approved: " + e.getAsMention()).build()
                                ).queue();
                            });
                        } catch (IOException ioe) {
                            LOGGER.error("An error occurred while creating Icon for an emote file.", ioe);
                        }
                        f.delete();
                    });
                } else if (id.equals("emote-add-deny")) {
                    event.deferEdit().queue();
                    event.getHook().editOriginalComponents(List.of()).queue();
                    event.getHook().editOriginal("Request was denied").queue();
                }
            } else {
                event.reply("You do not have permission to respond to this.").setEphemeral(true).queue();
            }
        }
    }
}
