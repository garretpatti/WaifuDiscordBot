package com.github.waifu;
import com.github.waifu.commands.*;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.Random;

import java.util.function.Consumer;
import javax.annotation.Nonnull;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.json.JSONObject;

import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import com.github.waifu.commands.TenorHandler;
import com.google.gson.JsonArray;

public class ResponseCenter extends ListenerAdapter{
    private static final ResponseCenter singleton = new ResponseCenter();
    private static Random randomGen = new Random();


    private static JsonArray commandList;
    private static JsonArray reactionList;

    public static ResponseCenter getSingleton() { return singleton; }

    private ResponseCenter() {
        loadJSON();
    }

    private void loadJSON() {
        try {
            String path = App.class.getResource("/Responses.json").getPath();
            JsonObject commandTree = JsonParser.parseReader(new FileReader(path)).getAsJsonObject();
            commandList = commandTree.getAsJsonArray("commands");
            reactionList = commandTree.getAsJsonArray("reactions");
        } catch (FileNotFoundException e) {
            System.out.println("Responses.json was not found.");
        } catch (Exception e) {
            System.out.println("Something is likely wrong with the Responses.json file.");
            e.printStackTrace();
        }
    }


    @Override
    public void onMessageReceived(@Nonnull MessageReceivedEvent event) {
        Message msg = event.getMessage();
        MessageChannel channel = event.getChannel();
        if (channel.getType().equals(ChannelType.TEXT)) {
            TextChannel textChannel = (TextChannel) channel;
            if (!msg.getContentRaw().equals("")) {
                String strMsg = msg.getContentRaw().trim();
                Consumer<JSONObject> memeResponseConsumer = r -> {
                    String url = r.getJSONArray("results").getJSONObject(0).getJSONArray("media").getJSONObject(0).getJSONObject("tinygif").getString("url");
                    textChannel.sendMessage(url).queue();
                };
                Consumer<Exception> memeErrorConsumer = e -> System.out.println("Error - " + e.getMessage());
                Consumer<String> textResponseConsumer = r -> {
                    if (r != null && r != "")
                        textChannel.sendMessage(r).queue();
                };

                //search the message for commands
                for (JsonElement command : commandList) {
                    try {
                        String commandName = command.getAsJsonObject().get("command").getAsString();
                        if (command.isJsonObject() && strMsg.contains(commandName)) {
                            switch (command.getAsJsonObject().get("handler").getAsString().toLowerCase()) {
                                case "simple":
                                    JsonArray simpleResponses = command.getAsJsonObject().get("response_list").getAsJsonArray();
                                    String acceptedChannel = command.getAsJsonObject().get("channels").getAsString().toLowerCase();
                                    if (acceptedChannel.contentEquals("all") || (textChannel.isNSFW() && acceptedChannel.contentEquals("nsfw")))
                                        SimpleHandler.respond(simpleResponses, textResponseConsumer, memeErrorConsumer);
                                    break;
                                case "tenor": //currently doesn't accept parameters
                                    JsonElement tenorInputs = command.getAsJsonObject().get("searches");
                                    TenorHandler.getSearchResults(tenorInputs.getAsJsonArray(), memeResponseConsumer, memeErrorConsumer);
                                    break;
                                default:
                                    System.out.println("handler not implemented");

                            }
                        }
                    } catch (Exception e) {
                        memeErrorConsumer.accept(e);
                    }
                }
                //search the message for reaction keywords
                for (JsonElement reaction : reactionList) {
                    try {
                        String reactionKW = reaction.getAsJsonObject().get("keyword").getAsString().toLowerCase();
                        String acceptedChannel = reaction.getAsJsonObject().get("channels").getAsString().toLowerCase();
                        if(strMsg.contains(reactionKW) && (acceptedChannel.contentEquals("all") || (textChannel.isNSFW() && acceptedChannel.contentEquals("nsfw")))) {
                            JsonArray possibleReactions = reaction.getAsJsonObject().get("reaction_list").getAsJsonArray();
                            String selectedReaction = possibleReactions.get(randomGen.nextInt(possibleReactions.size())).getAsString();
                            msg.addReaction(selectedReaction).queue();
                        }
                    } catch (Exception e) {
                        memeErrorConsumer.accept(e);
                    }

                }
            }
        }
    }
}