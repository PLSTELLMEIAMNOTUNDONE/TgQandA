package main.bots;

import ch.qos.logback.classic.net.LoggingEventPreSerializationTransformer;
import lombok.Setter;
import main.logs.Logger;
import main.methods.CommandHandler;
import main.methods.Executable;
import org.springframework.beans.factory.annotation.Autowired;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.GetFile;
import org.telegram.telegrambots.meta.api.methods.send.SendMediaGroup;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.methods.updates.SetWebhook;
import org.telegram.telegrambots.meta.api.objects.*;
import org.telegram.telegrambots.meta.api.objects.media.InputMedia;
import org.telegram.telegrambots.meta.api.objects.media.InputMediaPhoto;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.starter.SpringWebhookBot;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;

public class TgBot extends SpringWebhookBot {
    @Setter
    private String botToken;
    @Setter
    private String botUsername;
    @Setter
    private String botPath;
    @Setter
    @Autowired
    private CommandHandler commandHandler;

    public TgBot(SetWebhook setWebhook) {
        super(setWebhook);
    }

    @Override
    public String getBotToken() {
        return botToken;
    }


    public BotApiMethod<?> doAll(List<Executable> ans) {

        try {
            for (int i = 0; i < ans.size() - 1; i++) {


                if(ans.get(i).hasSendMessage())execute(ans.get(i).sendMessage());
                if(ans.get(i).hasSendPhoto())execute(ans.get(i).sendPhoto());

            }
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
        return ans.get(ans.size() - 1).sendMessage();
    }

    @Override
    public BotApiMethod<?> onWebhookUpdateReceived(Update update) {
        Logger.log(update, "", "onWebhookUpdateReceived");
        if (update.hasMessage() && (update.getMessage().hasText() && (update.getMessage().getText().charAt(0) == '/')
                || (update.getMessage().hasPhoto() && update.getMessage().getCaption() != null && update.getMessage().getCaption().charAt(0) == '/'))) {
            Message message = update.getMessage();

            if (message.hasPhoto()) {
                message.setText(message.getCaption());
                String f_id = Objects.requireNonNull(message.getPhoto().stream().max(Comparator.comparing(PhotoSize::getFileSize))
                        .orElse(null)).getFileId();

                GetFile getFile = new GetFile(f_id);
                String paths = null;

                try {
                    File file = execute(getFile);
                    downloadFile(file, new java.io.File("src/main/photos/" + file.getFileId() + ".png"));
                    paths = "src/main/photos/" + file.getFileId() + ".png";

                } catch (TelegramApiException e) {
                    throw new RuntimeException(e);
                }



                return doAll(commandHandler.HandleCommand(message, paths));

            }
            return doAll(commandHandler.HandleCommand(message));
        }
        return null;
    }

    @Override
    public String getBotPath() {
        return botPath;
    }

    @Override
    public String getBotUsername() {
        return botUsername;
    }
}
