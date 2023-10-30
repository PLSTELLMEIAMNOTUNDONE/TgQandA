package main.methods;

import jakarta.annotation.PostConstruct;
import lombok.Setter;
import main.logs.Logger;
import main.models.Photo;
import main.models.Question;
import main.models.Tag;
import main.services.QuestionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendDocument;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Message;

import java.io.File;
import java.util.*;

@Component
public class CommandHandler {

    private final QuestionService qs;

    public CommandHandler(@Autowired QuestionService qs) {
        this.qs = qs;
    }

    @Setter
    private HashMap<String, Command> methods;


    /**
     * @param message, already valid
     * @return answer to command
     */
    public List<Executable> HandleCommand(Message message, String... params) {
        String methodName = getCommandName(message.getText());
        return methods.getOrDefault(methodName,
                new Command() {
                    @Override
                    public List<Executable> execute(Message message) {
                        List<Executable> list = new ArrayList<>();
                        list.add(new Executable(new SendMessage(message.getChatId() + "", "такого метода нету(((")));
                        return list;
                    }

                    @Override
                    public String description() {
                        return null;
                    }
                }).execute(message, params);


    }

    @PostConstruct
    public void initMethods() {


        methods = new HashMap<>();
        methods.put("add", new Command() {
            @Override
            public List<Executable> execute(Message message) {
                ArrayList<Executable> list = new ArrayList<>();
                String chatId = String.valueOf(message.getChatId());

                String text = message.getText();
                Question question = parseQuestion(text);
                question = qs.insertQuestion(question);


                for (String tag : getTags(text.replaceFirst("/" + getCommandName(text)
                        + question.getQuestion()
                        + ";"
                        + question.getAnswer()
                        + ";", ""))) {
                    qs.addTag(tag, question);
                }
                Logger.log(message.toString(),
                        list,
                        "execute");


                list.add(new Executable(new SendMessage(chatId, "Вопрос добавлен")));
                return list;
            }

            @Override
            public List<Executable> execute(Message message, String... photoPath) {

                var ans = execute(message);
                if (photoPath.length == 0) return ans;
                Question question = parseQuestion(message.getText());
                Question questionFromDb = qs.getQuestionByText(question.getQuestion()).get(0);
                qs.addPhoto(questionFromDb, photoPath);
                Logger.log(message.toString() + Arrays.toString(photoPath),
                        ans,
                        "execute");
                return ans;
            }

            private List<String> getTags(String text) {

                String[] ar = text.split(";");
                List<String> tags = new ArrayList<>();
                for (String s : ar) {
                    tags.add(s.replaceAll(" ", ""));
                }
                Logger.log(text, tags, "getTags");
                return tags;
            }

            //
//
//            private String getQuestion(String text) {
//
//                String withoutCommandName = text.replaceFirst("/" + getCommandName(text), "");
//                StringBuilder str = new StringBuilder();
//                int i = 1;
//                while (i < withoutCommandName.length()
//                        && withoutCommandName.charAt(i) != ';') {
//                    str.append(withoutCommandName.charAt(i));
//                    i++;
//                }
//                Logger.log(text, str.toString(), "getQuestion");
//                return str.toString();
//            }
//
//            private String getAnswer(String text) {
//                String without = text.replaceFirst("/" + getCommandName(text) + getQuestion(text), "");
//                StringBuilder str = new StringBuilder();
//                int i = 1;
//                while (i < without.length()
//                        && without.charAt(i) != ';') {
//                    str.append(without.charAt(i));
//                    i++;
//                }
//                Logger.log(text, str.toString(), "getAnswer");
//                return str.toString();
//            }
            private Question parseQuestion(String text) {
                Question q = new Question();
                StringBuilder question = new StringBuilder(),
                        answer = new StringBuilder(),
                        tags = new StringBuilder();
                text = text.replaceFirst("/" + getCommandName(text), "");
                int i = 0;
                while (i < text.length() && text.charAt(i) != ';') {
                    question.append(text.charAt(i));
                    i++;

                }
                i++;
                while (i < text.length() && text.charAt(i) != ';') {
                    answer.append(text.charAt(i));
                    i++;

                }
                i++;

                q.setQuestion(question.toString());
                q.setAnswer(answer.toString());
                return q;
            }

            @Override
            public String description() {
                return "добавить вопрос \n " +
                        "Список параметров: Вопрос, опционально ответ, опционально скриншот, опционально теги \n" +
                        "каждый параметр должен быть отделен точкой с запятой и передаваться в указанном порядке";
            }

        });
        methods.put("getWithTags", new Command() {
            @Override
            public List<Executable> execute(Message message) {
                String text = message.getText();
                List<Executable> ans = new ArrayList<>();
                HashSet<Question> questionHashSet = qs.getQuestionsWithTags(
                        qs.getTagsByText(getTags(text))
                );
                SendMessage sendMessage = new SendMessage(message.getChatId() + "", "Найденные вопросы");
                ans.add(new Executable(sendMessage));
                for (Question question : questionHashSet) {
                    List<Photo> photos = qs.getPhotoByQuestionId(question);
                    for (Photo photo : photos) {

                        InputFile file = new InputFile(new File(photo.getPath()));
                        SendPhoto photoMessage = new SendPhoto(message.getChatId() + "", file);

                        ans.add(new Executable(photoMessage));
                    }

                    SendMessage questionMessage = new SendMessage(message.getChatId() + "", parseQuestion(question));
                    ans.add(new Executable(questionMessage));
                }
                return ans;


            }

            private String getCommandName(String text) {


                StringBuilder str = new StringBuilder();
                int i = 1;
                while (i < text.length() &&
                        !Character.isWhitespace(text.charAt(i))) {
                    str.append(text.charAt(i));
                    i++;
                }
                Logger.log(text, str.toString(), "getCommandName");
                return str.toString();
            }

            private List<String> getTags(String text) {
                if (text.equals("getWithTags")) return new ArrayList<>();
                String onlyTags = text.replaceFirst("/" + getCommandName(text), "");
                String[] ar = onlyTags.split(";");
                List<String> tags = new ArrayList<>();
                for (String s : ar) {
                    tags.add(s.strip());
                }
                Logger.log(text, tags, "getTags");
                return tags;
            }

            @Override
            public String description() {
                return "Найти метод по тегам + \n" +
                        "Список параметров: Названия тегов, каждое должно быть отделено от предыдущего точкой с запятой";
            }
        });
        methods.put("addAns", new Command() {
            @Override
            public List<Executable> execute(Message message) {
               List<Executable> list = new ArrayList<>();
               list.add(singleExecute(message));
               Logger.log(message, list.get(0), "addAns");
               return list;

            }

            private  Executable singleExecute(Message message) {
                String[] args = message.getText().split(";");

                Long id = getIdFromText(args[0]);
                Optional<Question> questionFromDatabase = qs.getQuestionById(id);
                if (questionFromDatabase.isEmpty()) {
                    return new Executable(new SendMessage(message.getChatId()+"", "Вопроса с таким номером не существует((("));
                }
                Question q = questionFromDatabase.get();
                q.setAnswer(args[1]);
                qs.updateQuestion(q);

                return new Executable(new SendMessage(message.getChatId()+"", "Вопрос изменен"));
            }
            private Long getIdFromText(String text) {
                String input = text;
                text =  text.replaceFirst("/addAns", "").strip();
                for (int i = 0; i < text.length(); i++) {
                    if(!Character.isDigit(text.charAt(i)))return -1L;
                }
                Logger.log(input, text,"getIdFromText");
                return Long.parseLong(text);
            }

            @Override
            public String description() {
                return "Добавить ответы к вопросу \n" +
                        " Список параметров: Номер вопроса; текст ответа" + " \n" +
                        "каждый параметр должен быть отделен точкой с запятой и передаваться в указанном порядке";
            }
        });
        // not sutt
        methods.put("addTags", new Command() {
            @Override
            public List<Executable> execute(Message message) {
                List<Executable> list = new ArrayList<>();
                list.add(singleExecute(message));
                Logger.log(message, list.get(0), "addTags");
                return list;

            }

            private  Executable singleExecute(Message message) {
                String[] args = message.getText().split(";");

                Long id = getIdFromText(args[0]);
                Optional<Question> questionFromDatabase = qs.getQuestionById(id);
                if (questionFromDatabase.isEmpty()) {
                    return new Executable(new SendMessage(message.getChatId()+"", "Вопроса с таким номером не существует((("));
                }
                Question q = questionFromDatabase.get();
                for (int i = 1; i < args.length; i++) {
                    qs.addTag(args[i].strip(), q);
                }
                qs.updateQuestion(q);

                return new Executable(new SendMessage(message.getChatId()+"", "Вопрос изменен"));
            }
            private Long getIdFromText(String text) {
                String input = text;
                text =  text.replaceFirst("/addTags", "").strip();
                for (int i = 0; i < text.length(); i++) {
                    if(!Character.isDigit(text.charAt(i)))return -1L;
                }
                Logger.log(input, text,"getIdFromText");
                return Long.parseLong(text);
            }

            @Override
            public String description() {
                return "Добавить ответы к вопросу \n" +
                        " Список параметров: Номер вопроса; теги, перечисленные через точку с запятой" + " \n" +
                        "каждый параметр должен быть отделен точкой с запятой и передаваться в указанном порядке";
            }
        });
        // not sutt
        methods.put("addPhoto", new Command() {


            @Override
            public List<Executable> execute(Message message, String... photoPath) {
                List<Executable> list = new ArrayList<>();
                list.add(singleExecute(message,  photoPath));
                Logger.log(message, list.get(0), "addPhoto");
                return list;
            }

            private  Executable singleExecute(Message message,  String... photoPath) {


                Long id = getIdFromText(message.getText());
                Optional<Question> questionFromDatabase = qs.getQuestionById(id);
                if (questionFromDatabase.isEmpty()) {
                    return new Executable(new SendMessage(message.getChatId()+"", "Вопроса с таким номером не существует((("));
                }
                Question q = questionFromDatabase.get();
                qs.addPhoto(q, photoPath);
                qs.updateQuestion(q);

                return new Executable(new SendMessage(message.getChatId()+"", "Вопрос изменен"));
            }


            private Long getIdFromText(String text) {
                String input = text;
                text =  text.replaceFirst("/addPhoto", "").strip();
                for (int i = 0; i < text.length(); i++) {
                    if(!Character.isDigit(text.charAt(i)))return -1L;
                }
                Logger.log(input, text,"getIdFromText");
                return Long.parseLong(text);
            }

            @Override
            public String description() {
                return "Добавить ответы к вопросу \n" +
                        " Список параметров: Номер вопроса;" + " \n" +
                        "каждый параметр должен быть отделен точкой с запятой и передаваться в указанном порядке";
            }
        });
        methods.put("help", new Command() {
            @Override
            public List<Executable> execute(Message message) {
                StringBuilder ans = new StringBuilder();
                for (String method : methods.keySet()) {
                    ans.append("/" + method + "\t" + methods.get(method).description() + "\n");
                }
                SendMessage sendMessage = new SendMessage(message.getChatId() + "",
                        ans.toString());
                List<Executable> list = new ArrayList<>();

                list.add(new Executable(sendMessage));
                return list;
            }

            @Override
            public String description() {
                return "Список возможностей бота";
            }
        });


    }


    private String getCommandName(String text) {
        if (methods.containsKey(text)) return "";

        StringBuilder str = new StringBuilder();
        int i = 1;
        while (i < text.length() &&
                !Character.isWhitespace(text.charAt(i))) {
            str.append(text.charAt(i));
            i++;
        }
        Logger.log(text, str.toString(), "getCommandName");
        return str.toString();
    }

    private String parseQuestion(Question q) {
        List<String> tagsOfQ = qs.getTagByQuestionId(q).stream().map(Tag::getTag).toList();
        return "Номер вопроса: " + q.getId() + "\n" +
                "Вопрос: " + q.getQuestion() + "\n" +
                "Ответ: " + q.getAnswer() + "\n" +
                "Теги: " + tagsOfQ.toString();
    }

}
/*
 *
 *
 * добавить возможность загружать несколько фото
 * добавить возможность добавлять теги ответы фото
 * сделать так чтобы с вопросом выводились и его теги
 *
 *
 * */