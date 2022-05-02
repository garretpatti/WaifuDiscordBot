package com.github.waifu.interactions;

import com.github.waifu.interactions.buttons.IButtonInteraction;
import com.github.waifu.interactions.slash.*;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.internal.utils.Checks;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class InteractionCenter extends ListenerAdapter {

    public static final Logger LOGGER = LoggerFactory.getLogger(InteractionCenter.class);
    private static final InteractionCenter singleton = new InteractionCenter();

    private final Map<String, ISlashInteraction> slashHandlers = new HashMap<>();
    private final Map<String, IButtonInteraction> buttonHandlers = new HashMap<>();

    private InteractionCenter() {}

    public static InteractionCenter getSingleton() { return singleton; }

    // should be called after bot starts up
    public void registerCommands(@Nonnull JDA bot) {
        LOGGER.info("Registering slash commands for WDO");
        // Add a new instance of your command handler here to register it
        List.of(
                new SlashNh(),
                new SlashBasicResponse("ping", "Pong!", "Ping test"),
                new SlashBasicResponse("bing", "Bong!", "Bing bong!"),
                new SlashBaseTenorSearch("smashing", "Smashing!", "nigel thornberry smashing"),
                new SlashBaseTenorSearch("cagemebro", "I'm going to steal the Declaration of Independence", "nick cage"),
                new SlashBaseTenorSearch("deuces", "Peace bitches", "deuces"),
                new SlashPokemon(),
                new SlashBasicResponse("kanyon", "bruh", "Bing bong!"),
                new SlashMagic8(),
                new SlashEmote()
        ).forEach(t -> {
            // TODO Discord uses the crazy regex as the naming rule for commands
            // ^[-_\p{L}\p{N}\p{sc=Deva}\p{sc=Thai}]{1,32}$
            try {
                Checks.notBlank(t.getName(),"Command name");
            }
            catch (IllegalStateException e) {
                LOGGER.error(
                    String.format(
                        "A command of Type %s with no name was provided. It will not be registered.",
                        t.getClass().getSimpleName()),
                    e);
                return;
            }

            List<Long> guilds = t.getGuilds();
            if (Optional.ofNullable(guilds).orElse(List.of()).isEmpty()) {
                bot.upsertCommand(t.getCommand()).queue(command -> {
                    LOGGER.info(String.format("Global command %s registered", command.getName()));
                });
            }
            else {
                guilds.forEach( g -> {
                    Guild guild = bot.getGuildById(g);
                    if (guild != null) {
                        guild.upsertCommand(t.getCommand()).queue(command -> {
                            LOGGER.info(String.format("Command %s registered for guild %d", command.getName(), g));
                        });
                    }
                });
            }
            slashHandlers.put(t.getName(), t);

            if (t instanceof IButtonInteraction handler) {
                handler.getButtons().forEach(b -> {
                    LOGGER.info(String.format("Registering button handler %s for command %s",
                            b.getId(),
                            t.getName()));
                    buttonHandlers.put(b.getId(), handler);
                });
            }
        });
    }

    @Override
    public void onSlashCommandInteraction(@Nonnull SlashCommandInteractionEvent event) {
        LOGGER.debug("Capturing slash event for command " + event.getName());
        ISlashInteraction command = slashHandlers.get(event.getName());
        try {
            if (command != null) command.onCommand(event);
        }
        catch (Exception e) {
            LOGGER.error("An uncaught exception was thrown while processing a slash command.", e);
        }
    }

    @Override
    public void onButtonInteraction(ButtonInteractionEvent event) {
        LOGGER.debug("Capturing button interaction event for button " + event.getComponentId());
        IButtonInteraction handler = buttonHandlers.get(event.getComponentId());
        try {
            if (handler != null) handler.onInteract(event);
        }
        catch (Exception e) {
            LOGGER.error("An uncaught exception was thrown while processing a button interaction.", e);
        }
    }
}
