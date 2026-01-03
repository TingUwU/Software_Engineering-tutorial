package Team5.example.breakfast_ordering.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer{
    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // 啟用簡單的訊息代理，客戶端訂閱的路徑前綴
        config.enableSimpleBroker("/topic");
        // 客戶端發送訊息給後端的路徑前綴
        config.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // 註冊 WebSocket 端點
        registry.addEndpoint("/ws")
                .setAllowedOrigins("https://breakfast-frontend.onrender.com") 
                .withSockJS(); // 啟用 SockJS 支援
    }    
}
