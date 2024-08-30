package com.soudry;

import java.util.EnumSet;

import javax.security.auth.login.LoginException;

import com.soudry.Listeners.DiceListener;
import com.soudry.Listeners.SlashCommands;

import io.github.cdimascio.dotenv.Dotenv;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.exceptions.InvalidTokenException;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.sharding.DefaultShardManagerBuilder;
import net.dv8tion.jda.api.sharding.ShardManager;

public class MunchDice {

    private final Dotenv config;

    private final ShardManager shardManager;

    MunchDice() throws LoginException, InvalidTokenException {
        config = Dotenv.load();
        String discordToken = config.get("TOKEN");
        DefaultShardManagerBuilder builder = DefaultShardManagerBuilder.createDefault(discordToken, 
        EnumSet.of(GatewayIntent.GUILD_MESSAGES, GatewayIntent.MESSAGE_CONTENT));
        builder.setStatus(OnlineStatus.ONLINE);
        builder.setActivity(Activity.playing("Munchy's Dungeons and Dragons"));
        
        this.shardManager = builder.build();
        
        shardManager.addEventListener(new DiceListener());
        // shardManager.addEventListener(new MyListener());
        for (JDA jda :  this.shardManager.getShards()) {
        shardManager.addEventListener(new SlashCommands(jda));
        }
    }

    public ShardManager getShardManager() {
         return this.shardManager;
    }

    public Dotenv geDotenv() {
        return this.config;
    }

    public static void main(String[] args) {
        try {
           new MunchDice();
        } catch (LoginException | InvalidTokenException i) {
            System.out.println("Error:  Provided bot token is invalid");
        } 
    }
}