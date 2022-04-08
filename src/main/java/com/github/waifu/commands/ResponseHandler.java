package com.github.waifu.commands;

import java.util.ArrayList;
import java.util.function.Consumer;

import javax.annotation.Nonnull;

import com.google.gson.JsonObject;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import com.google.gson.JsonElement;

public abstract class ResponseHandler {
    protected JsonObject responseObject;


    protected String responseKeyword;
    protected ArrayList<String> responseData = new ArrayList<String>();
    protected ArrayList<Long> acceptedChannels = new ArrayList<Long>();
    protected ArrayList<Long> acceptedGuilds = new ArrayList<Long>();
    protected boolean global;
    protected boolean channelAgnostic;
    protected boolean nsfw;


    public abstract void respond(@Nonnull MessageReceivedEvent event, Consumer<String> messageConsumer, Consumer<Exception> errorConsumer);

    ResponseHandler(JsonObject responseObject) {
        try {
            if (responseObject.get("keyword") != null) {
                responseKeyword = responseObject.get("keyword").toString();
            }
            else {
                throw new Exception("No Response Keyword found");
            }

            if (responseObject.get("data") != null) {
                for (JsonElement dataItem : responseObject.get("data").getAsJsonArray()) {
                    responseData.add(dataItem.toString());
                }
            }
            else { // delete this if a handler that doesn't need input is developed
                throw new Exception("No Response Data found");
            }
            if (responseObject.get("nsfw") != null) {
                nsfw = responseObject.get("nsfw").getAsBoolean();
            }
            else {
                nsfw = false;
            }
            if (responseObject.get("channels") != null) {
                channelAgnostic = false;
                if (responseObject.get("channels").isJsonArray()) {
                    for (JsonElement channel : responseObject.get("channels").getAsJsonArray()) {
                        acceptedChannels.add(channel.getAsLong());
                    }
                }
                else {
                    acceptedChannels.add(responseObject.get("channels").getAsLong());
                }
            }
            else {
                channelAgnostic = true;
            }
            if (responseObject.get("guilds") != null) {
                global = false;
                if (responseObject.get("guilds").isJsonArray()) {
                    for (JsonElement guild : responseObject.get("guilds").getAsJsonArray()) {
                        acceptedGuilds.add(guild.getAsLong());
                    }
                }
                else {
                    acceptedGuilds.add(responseObject.get("guilds").getAsLong());
                }
            }
            else {
                global = true;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String getKeyword() {
        return responseKeyword;
    }

    protected boolean responseConditionsMet(@Nonnull MessageReceivedEvent event) {
        if ((global || acceptedGuilds.contains(event.getGuild().getIdLong()))
         && (channelAgnostic || acceptedChannels.contains(event.getChannel().getIdLong()))
         && (!nsfw || event.getTextChannel().isNSFW())) {
            return true;
        }
        else {
            return false;
        }
    }
}
