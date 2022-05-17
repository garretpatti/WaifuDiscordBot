package com.github.waifu.interactions.slash;

import com.github.waifu.chat.ChatCenter;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;

import javax.annotation.Nonnull;
import java.util.List;

public class SlashRfR implements ISlashInteraction {

    @SlashCommand
    public static final SlashRfR cmd = new SlashRfR();

    @Nonnull
    @Override
    public String getName() {
        return "rfr";
    }

    @Nonnull
    @Override
    public CommandData getCommand() {
        return Commands.slash(this.getName(), "React-for-role mapping admin command.")
            .addSubcommands(
                new SubcommandData("add", "Add a message->role mapping, or change a pre-existing one")
                    .addOptions(
                        new OptionData(OptionType.STRING, "msg_id", "The message numeric ID.", true),
                        new OptionData(OptionType.ROLE, "role_id", "The role to map.", true)
                    ),
                new SubcommandData("rm", "Remove a message->role mapping.")
                    .addOptions(
                        new OptionData(OptionType.STRING, "msg_id", "The message numeric ID.", true),
                        new OptionData(OptionType.ROLE, "role_id", "The role to un-map.", true)
                    )
            );
    }

    @Override
    public List<Long> getGuilds() {
        return List.of(879891493840617543L);
    }

    @Override
    public void onCommand(@Nonnull SlashCommandInteractionEvent event) {
        if (event.isFromGuild()) {
            Long guildID = event.getGuild().getIdLong();
            Member member = event.getMember();
            if (member != null && member.hasPermission(Permission.MANAGE_ROLES)) {
                event.deferReply().queue();
                String sub = event.getSubcommandName();
                long msgID = event.getOption("msg_id", 0L, OptionMapping::getAsLong);
                Role role = event.getOption("role_id", OptionMapping::getAsRole);
                if ("add".equals(sub)) {
                    // TODO check message exists too
                    if (role != null && member.canInteract(role)) {
                        ChatCenter.addMapping(guildID, msgID, role.getIdLong());
                        event.getHook()
                            .sendMessage("Mapping was successfully added for role " + role.getName())
                            .queue();
                    }
                    else {
                        event.getHook()
                            .sendMessage("You can't add a mapping for a role equal or higher than yourself.")
                            .queue();
                    }
                }
                else if ("rm".equals(sub)) {
                    // TODO check message exists too
                    if (role != null && member.canInteract(role)) {
                        if (ChatCenter.removeMapping(guildID, msgID, role.getIdLong())) {
                            event.getHook()
                                .sendMessage("Mapping was successfully removed for role " + role.getName())
                                .queue();
                        }
                        else {
                            event.getHook()
                                .sendMessage("The message or role could not be found with those IDs")
                                .queue();
                        }
                    }
                    else {
                        event.getHook()
                            .sendMessage("You can't change a mapping for a role equal or higher than yourself.")
                            .queue();
                    }
                }
            }
            else {
                event.reply("You don't have permission to use this command.").setEphemeral(true).queue();
            }
        }
        else {
            event.reply("You can't use this here.").queue();
        }
    }
}
