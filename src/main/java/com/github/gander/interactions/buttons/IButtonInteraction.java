package com.github.gander.interactions.buttons;

import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.interactions.components.buttons.Button;

import java.util.List;

public interface IButtonInteraction {
    public abstract List<Button> getButtons();

    public abstract void onInteract(ButtonInteractionEvent event);
}
