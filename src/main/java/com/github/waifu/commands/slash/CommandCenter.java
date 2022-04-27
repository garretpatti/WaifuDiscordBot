package com.github.waifu.commands.slash;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class CommandCenter extends ListenerAdapter {

    public static final Logger LOGGER = LoggerFactory.getLogger(CommandCenter.class);
    private static final CommandCenter singleton = new CommandCenter();

    private final Map<String, ISlashHandler> slashHandlers = new HashMap<>();
    private final Map<String, IButtonInteraction> buttonHandlers = new HashMap<>();

    private CommandCenter() {}

    public static CommandCenter getSingleton() { return singleton; }

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
            try {
                // TODO Discord uses the crazy regex as the naming rule for commands
                // ^[-_\p{L}\p{N}\p{sc=Deva}\p{sc=Thai}]{1,32}$
                Optional.of(t.getName());
            }
            catch (NullPointerException npe) {
                LOGGER.warn(String.format("A command of Type %s with no name was provided. It will not be registered.",
                        t.getClass().getSimpleName()));
                return;
            }
            if (t.isGlobal()) {
                bot.upsertCommand(t.getCommand()).queue(l ->
                    Optional.ofNullable(t.getPrivileges()).orElse(Map.of()).forEach( (g, p) -> {
                        Guild guild = bot.getGuildById(g);
                        if (guild != null) l.updatePrivileges(guild, p).queue();
                    })
                );
            }
            else {
                Optional.ofNullable(t.getPrivileges()).orElse(Map.of()).forEach( (g, p) -> {
                    Guild guild = bot.getGuildById(g);
                    if (guild != null) {
                        guild.upsertCommand(t.getCommand()).queue(l -> l.updatePrivileges(guild, p).queue());
                    }
                });
            }
            LOGGER.info(String.format("Finished registering command %s", t.getName()));
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
        LOGGER.info("Interactions finished registering.");
    }

    @Override
    public void onSlashCommandInteraction(@Nonnull SlashCommandInteractionEvent event) {
        LOGGER.debug("Capturing slash event for command " + event.getName());
        ISlashHandler command = slashHandlers.get(event.getName());
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
