package main.logs;

import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;


@Component
public class Logger {

    private final static File file = new File("C:\\Users\\Dns\\IntelliJIDEAProjects\\TgQandA\\src\\main\\logs\\input.txt");
    private static int calls = 0;

    static class Log {
        String input;
        String output;
        String methodName;
        int call;


        public Log(String input, String output, String methodName, int call) {
            this.input = input;
            this.output = output;
            this.methodName = methodName;
            this.call = call;
            save(this);

        }


        @Override
        public String toString() {
            return "Log{" +
                    "input='" + input + "\n" +
                    ", output='" + output + "\n" +
                    ", methodName='" + methodName + "\n" +
                    '}';
        }

        public Log(String input, Exception e, String methodName, int call) {
            this.input = input;
            this.methodName = methodName;
            this.output = e.getMessage() + "\n" + Arrays.toString(e.getStackTrace());
            this.call = call;
            save(this);
        }
    }


    public static void log(String input, String output, String method) {
        new Log(input, output, method, calls++);
    }

    public static void log(String input, Exception output, String method) {
        new Log(input, output, method, calls++);
    }

    public static void log(Object input, Object output, Object method) {
        new Log(input.toString(), output.toString(), method.toString(), calls++);
    }
    private static final FileWriter fileWriter;
    static {
        try {
            fileWriter = new FileWriter(file);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    public static void save(Log log) {

        try {
            System.out.println("{\n call №" + log.call + "\n" + log.toString() + "}\n");

            fileWriter.write("{\n call №" + log.call + "\n" + log.toString() + "}\n");

        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }
}
