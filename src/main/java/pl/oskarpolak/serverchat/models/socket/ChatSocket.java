package pl.oskarpolak.serverchat.models.socket;

import org.springframework.boot.autoconfigure.security.SecurityProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import pl.oskarpolak.serverchat.models.UserModel;

import java.io.IOException;
import java.util.*;

@EnableWebSocket
@Configuration
public class ChatSocket extends TextWebSocketHandler implements WebSocketConfigurer {

    Map<String, UserModel> userList = Collections.synchronizedMap(new HashMap<>());

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry webSocketHandlerRegistry) {
        webSocketHandlerRegistry.addHandler(this, "/chat")
                .setAllowedOrigins("*");
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        session.sendMessage(new TextMessage("Hejo!"));
        session.sendMessage(new TextMessage("Twoja pierwsza wiadomość zostanie Twoim nickiem!"));
        userList.put(session.getId(), new UserModel(session));
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {

        UserModel userModel = userList.get(session.getId());



        if(userModel.getNick() == null){

            for(UserModel localNick : userList.values()){
                if(localNick.getNick() != null && localNick.getNick().equals(message.getPayload())){
                    userModel.getSession().sendMessage(new TextMessage("Nick zajęty"));
                    return;
                }
            }
            userModel.setNick(message.getPayload());
            userModel.getSession().sendMessage(
                    new TextMessage("Ustawiliśmy Twój nick!")
            );
            return;
        }

        userList.values().forEach(s -> {
                    try {
                        TextMessage newMessage = new TextMessage(userModel.getNick() + ": " + message.getPayload());
                        s.getSession().sendMessage(newMessage);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
       userList.remove(session.getId());
    }
}
