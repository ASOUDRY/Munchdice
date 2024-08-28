package com.soudry.Listeners;

import javax.annotation.Nonnull;

import net.dv8tion.jda.api.JDA;

// import net.dv8tion.jda.api.entities.SlashCommandData;
// import net.dv8tion.jda.api.events.interaction.command.SlashCommandEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.restaction.CommandListUpdateAction;
import net.dv8tion.jda.api.interactions.commands.build.Commands;

public class SlashCommands extends ListenerAdapter {

    private CommandListUpdateAction commands;

    public SlashCommands(JDA jda) {
        this.commands = jda.updateCommands();
    }

    public void addSlashCommands() {
        commands.addCommands(
            Commands.slash("say", "Makes the bot say something!")
        );
        commands.addCommands(
            Commands.slash("dragon", "Generates a dragon")
        );
        commands.queue();
    }

    @Override
    public void onSlashCommandInteraction(@Nonnull SlashCommandInteractionEvent event) {
        // Handle the "say" command
        if (event.getName().equals("say")) {
            event.reply("This is a static message from the `say` command!").queue();
        }
        // Handle the "dragon" command
        else if (event.getName().equals("dragon")) {
            event.reply("Here's a dragon for you! 🐉").queue();
        }
    }
}
