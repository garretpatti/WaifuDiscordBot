package com.github.waifu.commands;

import java.util.ArrayList;
import java.util.function.Consumer;

import javax.annotation.Nonnull;

import com.google.gson.JsonObject;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import com.google.gson.JsonElement;

public abstract class ResponseHandler {
    protected JsonObject responseObject;
    protected Consumer<String> messageConsumer;
    protected Consumer<Exception> errorConsumer;

    protected String responseKeyword;
    protected ArrayList<String> responseData = new ArrayList<String>();
    protected ArrayList<Long> acceptedChannels = new ArrayList<Long>();
    protected ArrayList<Long> acceptedGuilds = new ArrayList<Long>();
    protected boolean global;
    protected boolean channelAgnostic;
    protected boolean nsfw;


    public abstract void respond(@Nonnull MessageReceivedEvent event);

    public void buildResponse() {
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
            errorConsumer.accept(e);
        }
    }

    public String getKeyword() {
        return responseKeyword;
    }

    public boolean isNSFW() { return nsfw;}

    public boolean isGlobal() { return global;}

    public boolean isChannelAgnostic() { return channelAgnostic;}

    protected boolean responseConditionsMet(@Nonnull MessageReceivedEvent event) {
        if ((acceptedGuilds.contains(event.getGuild().getIdLong()) || global)
         && (acceptedChannels.contains(event.getChannel().getIdLong()) || channelAgnostic)
         && (event.getTextChannel().isNSFW() || !nsfw)) {
            return true;
        }
        else {
            return false;
        }
    }
}
