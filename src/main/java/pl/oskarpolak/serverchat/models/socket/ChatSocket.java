package pl.oskarpolak.serverchat.models.socket;

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
import java.util.ArrayList;
import java.util.List;

@EnableWebSocket
@Configuration
public class ChatSocket extends TextWebSocketHandler implements WebSocketConfigurer {

    List<UserModel> userList = new ArrayList<>();

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry webSocketHandlerRegistry) {
        webSocketHandlerRegistry.addHandler(this, "/chat")
                .setAllowedOrigins("*");
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        userList.add(new UserModel(session));
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {


        userList.forEach(s -> {
                    try {
                        s.getSession().sendMessage(message);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
       userList.remove(userList.stream()
               .filter(s -> s.getSession().equals(session))
               .findAny()
               .get());
    }
}
