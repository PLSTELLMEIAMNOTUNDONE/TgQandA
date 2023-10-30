package main.methods;

import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;

import java.util.ArrayList;
import java.util.List;

public interface Command {

    default List<Executable> execute(Message message) {
        Executable e = new Executable(new SendMessage(message.getChatId() + "", "что то пошло не так"));
        List<Executable> l = new ArrayList<>();
        l.add(e);
        return l;
    }

    ;

    default List<Executable> execute(Message message, String... photoPath) {
        return execute(message);
    }

    String description();
}
