package com.github.waifu.commands;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.privileges.CommandPrivilege;
import org.apache.commons.collections4.IteratorUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.StreamSupport;

public class SlashPokemon extends SlashCommandHandler {

    public static final Logger LOGGER = LoggerFactory.getLogger(SlashPokemon.class);

    public String getName() { return "pokemon"; }

    @Nonnull
    public CommandData getCommand() {
        return new CommandData(this.getName(), "Who's that Pokemon?")
            .addOption(OptionType.STRING, "get", "Get a specific pokemon by name or number", false);
    }

    public boolean isGlobal() { return false; }

    @Nullable
    public Map<Long, List<CommandPrivilege>> getPrivileges() {
        return Map.of(
            879891493840617543L, List.of()
        );
    }

    public void onCommand(SlashCommandEvent event) {
        event.deferReply().queue();
        new Thread(() -> {
            OptionMapping option = event.getOption("get");
            String get = (option == null ? String.valueOf((int)(Math.random() * 897 + 1)) : option.getAsString());
            String pokemonUrl = String.format("https://pokeapi.co/api/v2/pokemon/%s",get);
            String speciesUrl = String.format("https://pokeapi.co/api/v2/pokemon-species/%s", get);
            LOGGER.debug(String.format("Getting pokemon for url %s and option %s", pokemonUrl, option));
            // types

            // declare pokemon and species connections
            // try connecting
            // If the code is 404, this one wasn't found.
            // If the code is anything other than 404, 200, or 201, something else happened. Report error
            // else we got a good result. start pulling data
            HttpURLConnection pokemon = null;
            HttpURLConnection species = null;
            try {
                pokemon = requestGet(pokemonUrl);
                species = requestGet(speciesUrl);

                // Handle failure
                int statusPokemon = pokemon.getResponseCode();
                int statusSpecies = species.getResponseCode();
                if (statusPokemon == HttpURLConnection.HTTP_NOT_FOUND || statusSpecies == HttpURLConnection.HTTP_NOT_FOUND) {
                    event.getHook().sendMessage(String.format("Sorry Senpai, I couldn't find anything for pokemon %s", get)).queue();
                } else if (statusPokemon != HttpURLConnection.HTTP_OK && statusPokemon != HttpURLConnection.HTTP_CREATED) {
                    event.getHook().sendMessage("There was an issue connecting to the server. Please report this to Spitfyre.").queue();
                    LOGGER.warn(String.format("Error codes %d and %d were returned from PokeAPI. This command will not be fulfilled.", statusPokemon, statusSpecies));
                } else {
                    JSONObject pokemonData = parser(pokemon);
                    JSONObject speciesData = parser(species);
                    String name = "";
                    for (Object entry : speciesData.getJSONArray("names")) {
                        JSONObject nameEntry = (JSONObject) entry;
                        if (nameEntry.getJSONObject("language").getString("name").equals("en")) {
                            name = nameEntry.getString("name");
                            break;
                        }
                    }
                    int dexNum = 0;
                    for (Object entry : speciesData.getJSONArray("pokedex_numbers")) {
                        JSONObject dexEntry = (JSONObject) entry;
                        if (dexEntry.getJSONObject("pokedex").getString("name").equals("national")) {
                            dexNum = dexEntry.getInt("entry_number");
                            break;
                        }
                    }
                    String genus = "";
                    for (Object entry : speciesData.getJSONArray("genera")) {
                        JSONObject genusEntry = (JSONObject) entry;
                        if (genusEntry.getJSONObject("language").getString("name").equals("en")) {
                            genus = genusEntry.getString("genus");
                            break;
                        }
                    }
                    String desc = "";
                    List<Object> descList = IteratorUtils.toList(StreamSupport.stream(speciesData.getJSONArray("flavor_text_entries").spliterator(), true).filter(
                        obj -> {
                            JSONObject entry = (JSONObject) obj;
                            return entry.getJSONObject("language").getString("name").equals("en");
                        }
                    ).iterator());
                    desc = ((JSONObject)descList.get((int)(Math.random() * descList.size()))).getString("flavor_text");
                    String spriteURL = pokemonData.getJSONObject("sprites").getJSONObject("other").getJSONObject("home").getString("front_default");

                    MessageEmbed embed = new EmbedBuilder()
                            .setTitle(String.format("Pokedex Entry #%d", dexNum))
                            .setImage(spriteURL)
                            .addField(name, genus, false)
                            .addField("Description", desc.replaceAll("[\f\n]", " "), false)
                            .build();
                    event.getHook().sendMessageEmbeds(embed).queue();
                }
            } catch (IOException ioe) {
                event.getHook().sendMessage("Sorry Senpai, something happened while trying to find that.").queue();
                LOGGER.warn("An error occurred while retrieving the page from PokeAPI.", ioe);
            } catch (NullPointerException | JSONException err) {
                event.getHook().sendMessage("Some data was missing from the site. Please report this to Spitfyre.").queue();
                LOGGER.warn("A key or value was missing from an expected result from PokeAPI. Please check the endpoint version and verify expected results.", err);
            } finally {
                if (pokemon != null) {
                    pokemon.disconnect();
                }
                if (species != null) {
                    species.disconnect();
                }
            }
        }).start();
    }

    private HttpURLConnection requestGet(String url) throws IOException {
        HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
        // Get request
        connection.setRequestMethod("GET");
        connection.setRequestProperty("Content-Type", "text/html");
        connection.setRequestProperty("Accept", "text/html");
        connection.setRequestProperty("Content-Type", "text/html; charset=UTF-8");
        int statusCode = connection.getResponseCode();
        return connection;
    }

    /**
     * Parse the response into JSONObject
     */
    private static JSONObject parser(HttpURLConnection connection) {
        if (connection == null) { return null; }
        char[] buffer = new char[4096];
        int n;
        try (InputStream stream = new BufferedInputStream(connection.getInputStream())) {
            InputStreamReader reader = new InputStreamReader(stream, StandardCharsets.UTF_8);
            StringWriter writer = new StringWriter();
            while ((n = reader.read(buffer)) != -1) {
                writer.write(buffer, 0, n);
            }
            return new JSONObject(writer.toString());
        } catch (IOException ioe) {

        }
        return null;
    }
}
