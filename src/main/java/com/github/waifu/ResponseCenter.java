package com.github.waifu;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;

import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.Nonnull;

import com.google.gson.JsonElement;
import com.google.gson.JsonIOException;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonArray;

public class ResponseCenter {
    private static final ResponseCenter singleton = new ResponseCenter();

    private static JsonObject commandTree;
    private static JsonArray commandList;
    private static JsonArray reactionList;

    public static ResponseCenter getSingleton() { return singleton; }

    private ResponseCenter() {
        LoadJSON();
    }

    private void LoadJSON() {
        try {
            String path = App.class.getResource("/Responses.json").getPath();
            commandTree = JsonParser.parseReader(new FileReader(path)).getAsJsonObject();
            commandList = commandTree.getAsJsonArray("bang_commands");
        } catch (FileNotFoundException e) {
            System.out.println("BangCommands.json was not found.");
        } catch (Exception e) {
            System.out.println("Something is likely wrong with the BangCommands.json file.");
            e.printStackTrace();
        }
    }

    public void HandleCommands(String message) {
        for (JsonElement command : commandList) {
            String commandName = command.getAsJsonObject().get("command").getAsString();
            if (command.isJsonObject() && message.contains(commandName)) {
                switch (command.getAsJsonObject().get("handler").getAsString()) {
                    case "simple":

                }
            }
        }
    }
}