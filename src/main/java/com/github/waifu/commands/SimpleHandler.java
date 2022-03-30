package com.github.waifu.commands;

import com.google.gson.JsonArray;
import java.util.Random;
import java.util.function.Consumer;
/*
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

public class SimpleHandler {
    private static Random randomGen = new Random();
    public static void respond(JsonArray responses, Consumer<String> message, Consumer<Exception> error) {
        new Thread(() -> {
            try {
                String response = responses.get(randomGen.nextInt(responses.size())).getAsString();
                message.accept(response);
            }
            catch (Exception e) {
                error.accept(e);
            }
        }).start();
        
    }
}
