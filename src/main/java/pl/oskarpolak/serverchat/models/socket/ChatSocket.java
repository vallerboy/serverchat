package pl.oskarpolak.serverchat.models.socket;

import com.google.gson.reflect.TypeToken;
import org.springframework.boot.autoconfigure.security.SecurityProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import pl.oskarpolak.serverchat.models.MessageFactory;
import pl.oskarpolak.serverchat.models.UserModel;

import java.io.IOException;
import java.lang.reflect.Type;
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

        Type factory = new TypeToken<MessageFactory>() {}.getType();
        MessageFactory factoryCreated = MessageFactory.GSON.fromJson(message.getPayload(), factory);

        MessageFactory factoryNewMessage;

        switch (factoryCreated.getMessageType()){
            case SEND_MESSAGE: {
                //todo obórka nicku
                factoryNewMessage = new MessageFactory();
                factoryNewMessage.setMessage(message.getPayload());
                factoryNewMessage.setMessageType(MessageFactory.MessageType.SEND_MESSAGE);
                sendMessageToAll(factoryNewMessage);
                break;
            }
        }

    }

    private String convertFactoryToString(MessageFactory factory){
      return MessageFactory.GSON.toJson(factory);
    }

    public void sendMessageToAll(MessageFactory factory){
        for(UserModel user : userList.values()){
            try {
                user.getSession().sendMessage(new TextMessage(convertFactoryToString(factory)));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
       userList.remove(session.getId());
    }
}
