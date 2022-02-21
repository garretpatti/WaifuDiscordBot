package com.github.waifu;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.github.waifu.commands.TenorHandler;

import org.json.JSONObject;

import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class MessageHandler {
    public static void respond(MessageReceivedEvent event){
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

                if (strMsg.equals("!ping")) {
                    textChannel.sendMessage("Pong!").queue();
                }
                else if (strMsg.equals("!bing")) {
                    textChannel.sendMessage("Bong!").queue();
                }
                else if (strMsg.contains("!cagemebro")) {
                    TenorHandler.getSearchResults("nick cage", memeResponseConsumer, memeErrorConsumer);
                }
                else if (strMsg.contains("!smashing")) {
                    TenorHandler.getSearchResults("nigel thornberry smashing", memeResponseConsumer, memeErrorConsumer);
                }
                else if (strMsg.startsWith("!tenor") && strMsg.length() > 7) {
                    TenorHandler.getSearchResults(strMsg.substring(7), memeResponseConsumer, memeErrorConsumer);
                }
                else if (textChannel.isNSFW() && strMsg.startsWith("!nh") && strMsg.length() >= 10){
                    Matcher matcher = Pattern.compile("\\d{6}").matcher(strMsg.substring(4, 10));
                    if (matcher.find()) {
                        String ext = matcher.group(0);
                        new Thread(() -> {
                            String url = String.format("https://nhentai.net/g/%1$s", ext);
                            HttpURLConnection connection = null;
                            try {
                                connection = (HttpURLConnection) new URL(url).openConnection();
                                // Get request
                                connection.setDoInput(true);
                                connection.setDoOutput(true);
                                connection.setRequestMethod("GET");
                                connection.setRequestProperty("Content-Type", "text/html");
                                connection.setRequestProperty("Accept", "text/html");
                                connection.setRequestProperty("Content-Type", "text/html; charset=UTF-8");

                                // Handle failure
                                int statusCode = connection.getResponseCode();
                                if (statusCode != HttpURLConnection.HTTP_OK && statusCode != HttpURLConnection.HTTP_CREATED) {
                                    textChannel.sendMessage("Sorry Senpai, I couldn't find that one.").queue();
                                }
                                else {
                                    textChannel.sendMessage(url).queue();
                                }
                            }
                            catch (Exception ignored) {}
                            finally {
                                if (connection != null) {
                                    connection.disconnect();
                                }
                            }
                        }).start();
                    }
                }
            }
        }
    }
}
