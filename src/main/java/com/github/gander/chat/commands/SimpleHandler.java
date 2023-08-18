package com.github.gander.chat.commands;

import com.google.gson.JsonObject;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.Random;
import java.util.function.Consumer;

import javax.annotation.Nonnull;

/**
    Handler for Simple Commands that respond with a random entry from a list
    ------------------------------------------------------------------------
    The Json format for these commands is as follows:
    {
        "command":"commandname",
        "channels":<accepted channels>,     /either "all" or "nsfw"
        "handler":"simple",
        "response_list":[
            "list",
            "of",
            "responses"
        ]
    }
*/
public class SimpleHandler extends ResponseHandler{
    private static Random randomGen = new Random();

    public SimpleHandler(JsonObject responseObject) {
        super(responseObject);
    }

    @Override
    public void respond(@Nonnull MessageReceivedEvent event, Consumer<String> messageConsumer, Consumer<Exception> errorConsumer) {
        try {
            if (responseConditionsMet(event)) {
                String response = responseData.get(randomGen.nextInt(responseData.size()));
                messageConsumer.accept(response);
            }
        }
        catch (Exception e) {
            errorConsumer.accept(e);
        }
    }
}
