package main.controller;

import main.bots.TgBot;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.Update;

@RestController
public class BotController {
    private final TgBot bot;

    public BotController(@Autowired TgBot bot) {
        this.bot = bot;
    }

    @PostMapping("/")
    public BotApiMethod<?> getUpdate(@RequestBody Update update) {
        return bot.onWebhookUpdateReceived(update);

    }

}
