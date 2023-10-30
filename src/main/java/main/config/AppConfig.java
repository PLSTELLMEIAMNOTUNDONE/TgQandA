package main.config;

import main.bots.TgBot;
import main.methods.Command;
import main.methods.CommandHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.updates.SetWebhook;
import org.telegram.telegrambots.meta.api.objects.Message;

import java.util.HashMap;

@Configuration
public class AppConfig {
    @Autowired
    private final BotConfig botConfig;



    public AppConfig(BotConfig botConfig) {
        this.botConfig = botConfig;
    }

    @Bean
    public SetWebhook setWebhook() {
        return  SetWebhook.builder().url(botConfig.getBotPath()).build();
    }
    @Bean
    public TgBot tgBot(SetWebhook setWebhook) {
        TgBot bot = new TgBot(setWebhook);
        bot.setBotPath(botConfig.getBotPath());
        bot.setBotToken(botConfig.getBotToken());
        bot.setBotUsername(botConfig.getBotUsername());

        return bot;
    }

}
