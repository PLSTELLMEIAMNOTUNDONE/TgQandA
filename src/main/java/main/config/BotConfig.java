package main.config;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Getter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class BotConfig {
    @Value("${telegrambot.botToken}")
    String botToken;
    @Value("${telegrambot.botPath}")
    String botPath;
    @Value("${telegrambot.botUsername}")
    String botUsername;
}
