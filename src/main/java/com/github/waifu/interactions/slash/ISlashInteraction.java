package com.github.waifu.interactions.slash;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

/**
 * The interface that defines a Slash Command object and event handler.<br>
 *
 * Implementations of this class are not automatically considered
 * for registration by InteractionCenter. An instance field will
 * also have to be annotated with {@link SlashCommand} to be discovered.
 */
public interface ISlashInteraction {

    /**
     * @return the name ID that this handler will use to register to Discord
     *  and subscribe to usage of this command via the client.<br>
     *  <b>This value must be nonnull, non-empty, and unique.</b>
     */
    @Nonnull
    String getName();

    /**
     * The command structure object that is submitted to Discord when registered.
     * This object determines how your command will behave in the Discord client.
     * Refer to the docs in {@link CommandData} and
     * <a href="https://discord.com/developers/docs/interactions/application-commands">here</a>
     * on how to make advanced configurations with these.<br><br>
     *
     * <b>NOTE:</b> To ensure your command is properly registered and handled by
     * InteractionCenter, consider initializing your CommandData object with
     * {@link #getName()} as the name parameter.
     * @return a nonnull CommandData object to be registered to Discord.
     */
    @Nonnull
    CommandData getCommand();

    /**
     * The List of Guild long IDs to register this command to. A null or empty
     * list is evaluated as a global command.<br><br>
     *
     * <b>NOTE:</b> A global command can take up to an hour to propagate on the API backend.
     * If you are creating a command for testing, consider using this method to
     * return the long ID of a guild to test on.
     *
     * @return the List of Guild IDs to register this command to
     */
    @Nullable
    default List<Long> getGuilds() { return List.of(); }

    /**
     * This is where all the magic happens. When a command is submitted by a user
     * in the Discord client, a SlashCommandInteractionEvent is fired by the API
     * and captured by InteractionCenter. This event is then passed to the command
     * handler that is registered under the name on the event object.<br>
     * Here, your handler will respond to the interaction submitted to this bot,
     * processing arguments and behaving in whatever way you wish for this command
     * to.
     *
     * @param event the SlashCommandInteractionEvent fired by Discord for this
     *          interaction. This is never null, and is passed to your handler
     *          via the name supplied in {@link #getName()}
     */
    void onCommand(@Nonnull SlashCommandInteractionEvent event);
}
