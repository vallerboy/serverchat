package pl.oskarpolak.serverchat.models.socket;

import com.google.gson.reflect.TypeToken;
import org.springframework.boot.autoconfigure.security.SecurityProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.annotation.ModelFactory;
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
                //todo nick
                // Pod message znajduje sie normalna w swiecie wiadomosc
                factoryNewMessage = new MessageFactory();
                factoryNewMessage.setMessage(userModel.getNick() + ": " + factoryCreated.getMessage());
                factoryNewMessage.setMessageType(MessageFactory.MessageType.SEND_MESSAGE);
                sendMessageToAll(factoryNewMessage);
                break;
            }
            case SET_NICK: {
                // Pod message znajduje sie nick uzytkownika
                factoryNewMessage  = new MessageFactory();
                if(!isNickFree(factoryCreated.getMessage())){
                     factoryNewMessage.setMessageType(MessageFactory.MessageType.NICK_NOT_FREE);
                     factoryNewMessage.setMessage("Nick nie jest wolny przyjacielu!");
                     sendMessageToUser(userModel, factoryNewMessage);
                     return;
                }
                sendJoinPacket(factoryCreated.getMessage(), userModel);
                userModel.setNick(factoryCreated.getMessage());
                factoryNewMessage.setMessageType(MessageFactory.MessageType.SEND_MESSAGE);
                factoryNewMessage.setMessage("Ustawiłeś swój nick");
                sendMessageToUser(userModel, factoryNewMessage);
                break;
            }
        }

    }

    private boolean isNickFree(String nick){
        for (UserModel userModel : userList.values()) {
            if(userModel.getNick() != null && nick.equals(userModel.getNick())) {
                 return false;
            }
        }
        return true;
    }

    private String convertFactoryToString(MessageFactory factory){
      return MessageFactory.GSON.toJson(factory);
    }

    public void sendMessageToAllWithOutMe(UserModel model, MessageFactory factory){
        for(UserModel user : userList.values()){
            try {
                if(user.getSession().getId().equals(model.getSession().getId())){
                    continue;
                }
                user.getSession().sendMessage(new TextMessage(convertFactoryToString(factory)));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void sendJoinPacket(String nick, UserModel model){
        MessageFactory messageFactory = new MessageFactory();
        messageFactory.setMessageType(MessageFactory.MessageType.USER_JOIN);
        messageFactory.setMessage(nick);
        sendMessageToAll(messageFactory);
    }

    private void sendLeftPacket(String nick, UserModel model){
        MessageFactory messageFactory = new MessageFactory();
        messageFactory.setMessageType(MessageFactory.MessageType.USER_LEFT);
        messageFactory.setMessage(nick);
        sendMessageToAll(messageFactory);
    }

    public void sendMessageToUser(UserModel userModel, MessageFactory factory) {
        try {
            userModel.getSession().sendMessage(new TextMessage(convertFactoryToString(factory)));
        } catch (IOException e) {
            e.printStackTrace();
        }
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
        UserModel userModel = userList.get(session.getId());
        sendLeftPacket(userModel.getNick(), userModel);
        userList.remove(session.getId());
    }
}
