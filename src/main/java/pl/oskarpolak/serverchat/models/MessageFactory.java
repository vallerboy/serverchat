package pl.oskarpolak.serverchat.models;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.springframework.boot.json.GsonJsonParser;

public class MessageFactory {
    public static Gson GSON = new GsonBuilder().create();
    public enum MessageType{
        SEND_MESSAGE, USER_JOIN, USER_LEFT, GET_ALL_USERS;
    }

    private String message;
    private String author;

    public MessageFactory() {

    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }
}
