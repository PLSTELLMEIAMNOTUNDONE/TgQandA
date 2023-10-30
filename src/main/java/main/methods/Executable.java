package main.methods;

import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;

public class Executable {
    private SendPhoto sendPhoto;
    private SendMessage sendMessage;
    public boolean hasSendPhoto() {
        return this.sendPhoto != null;
    }

    public Executable(SendMessage sendMessage) {
        this.sendMessage = sendMessage;
    }

    public Executable(SendPhoto sendPhoto) {
        this.sendPhoto = sendPhoto;
    }

    public boolean hasSendMessage() {
        return this.sendMessage != null;
    }
    public SendPhoto sendPhoto() {
        return sendPhoto;
    }

    public void setSendPhoto(SendPhoto sendPhoto) {
        this.sendPhoto = sendPhoto;
    }

    public SendMessage sendMessage() {
        return sendMessage;
    }

    public void setSendMessage(SendMessage sendMessage) {
        this.sendMessage = sendMessage;
    }
}
