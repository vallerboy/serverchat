package pl.oskarpolak.serverchat.models;

import org.apache.catalina.User;
import org.springframework.web.socket.WebSocketSession;

public class UserModel {
    private String nick;
    private WebSocketSession session;

    public UserModel(WebSocketSession session){
        this.session = session;
        nick = null;
    }

    public String getNick() {
        return nick;
    }

    public void setNick(String nick) {
        this.nick = nick;
    }

    public WebSocketSession getSession() {
        return session;
    }

    public void setSession(WebSocketSession session) {
        this.session = session;
    }
}
