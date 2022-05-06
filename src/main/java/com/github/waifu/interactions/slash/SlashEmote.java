package com.github.waifu.interactions.slash;

import com.github.waifu.interactions.buttons.IButtonInteraction;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.List;

public class SlashEmote implements ISlashInteraction, IButtonInteraction {

    public static final Logger LOGGER = LoggerFactory.getLogger(SlashEmote.class);
    private static final Button APPROVE = Button.success("emote-add-approve", "Approve");
    private static final Button DENY = Button.danger("emote-add-deny", "Deny");

    @Nonnull
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
    public void onCommand(SlashCommandInteractionEvent event) {
        if (event.isFromGuild()) {
            event.deferReply().queue();
            String name = event.getOption("alias", "", OptionMapping::getAsString).toLowerCase();
            if (!name.equals("")) {
                event.getGuild().retrieveEmotes().queue(l -> {
                    // TODO check Guild for emote limit and availability
                    if (l.stream().anyMatch(e -> e != null && e.getName().equalsIgnoreCase(name))) {
                        event.getHook().sendMessage("An emote already exists by this name. Please choose another alias.").queue();
                    } else {
                        event.getOption("emote", OptionMapping::getAsAttachment).downloadToFile().thenAcceptAsync(f -> {
                            Member requestor = event.getMember();
                            try {
                                if (f.length() > 262144) {
                                    event.getHook().sendMessage(requestor.getAsMention() + " The submitted file is too large for an emote. The maximum size is 256.0 kb.").queue();
                                }
                                else if (event.getMember().hasPermission(Permission.MANAGE_EMOTES_AND_STICKERS)) {
                                    event.getGuild().createEmote(name, Icon.from(f)).queue(e -> {
                                        event.getHook().editOriginal(new MessageBuilder()
                                            .setContent(requestor.getAsMention() + " Emote successfully created: " + e.getAsMention()).build()).queue();
                                    });
                                } else {
                                    event.getHook()
                                        .sendFile(f)
                                        .setContent(String.format("Name: %s\nRequestor: %s", name, requestor.getAsMention()))
                                        .addActionRow(this.getButtons()).queue();
                                }
                            } catch (IOException ioe) {
                                LOGGER.error("An I/O error occurred while handling the attachment.", ioe);
                            } finally {
                                f.delete();
                            }
                        });
                    }
                });
            }
        }
    }

    @Override
    public List<Button> getButtons() {
        return List.of(APPROVE, DENY);
    }

    @Override
    public void onInteract(ButtonInteractionEvent event) {
        Guild guild = event.getGuild();
        if (guild != null) {
            Message request = event.getMessage();
            Member member = event.getMember();
            if (member != null && member.hasPermission(Permission.MANAGE_EMOTES_AND_STICKERS)) {
                event.deferEdit().queue();
                String id = event.getComponentId();
                User mention = request.getMentionedUsers().get(0);
                String requester = mention != null ? String.format("%s ", mention.getAsMention()) : "";
                String msg[] = request.getContentRaw().split("\n");
                String name = msg[0].substring(msg[0].indexOf(":") + 2);
                if (id.equals(APPROVE.getId())) {
                    event.getMessage().getAttachments().get(0).downloadToFile().thenAcceptAsync(f -> {
                        try {
                            event.getGuild().createEmote(name, Icon.from(f)).queue(e -> {
                                event.getHook().editOriginalComponents(List.of()).queue();
                                event.getHook().sendMessage(requester + "Request was approved: " + e.getAsMention()).queue();
                            });
                        } catch (IOException ioe) {
                            LOGGER.error("An error occurred while creating Icon for an emote file.", ioe);
                        } finally {
                            f.delete();
                        }
                    });
                } else if (id.equals(DENY.getId())) {
                    event.getHook().editOriginalComponents(List.of()).queue();
                    event.getHook().sendMessage(requester + "Request was denied").queue();
                }
            } else {
                event.reply("You do not have permission to respond to this.").setEphemeral(true).queue();
            }
        }
    }
}
