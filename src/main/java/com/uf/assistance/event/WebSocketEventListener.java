package com.uf.assistance.event;

import com.uf.assistance.domain.ai.AISubscription;
import com.uf.assistance.domain.chat.Chat;
import com.uf.assistance.domain.chat.MessageType;
import com.uf.assistance.domain.user.User;
import com.uf.assistance.service.AISubscriptionService;
import com.uf.assistance.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class WebSocketEventListener {
    private final SimpMessageSendingOperations messagingTemplate;
    private final AISubscriptionService aiSubscriptionService;
    private final UserService userService;

    @EventListener
    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());

        String username = (String) headerAccessor.getSessionAttributes().get("username");
        String aiSubscriptionIdStr = (String) headerAccessor.getSessionAttributes().get("aiSubscriptionId");

        if (username != null && aiSubscriptionIdStr != null) {
            Long aiSubscriptionId = Long.parseLong(aiSubscriptionIdStr);
            AISubscription aiSubscription = aiSubscriptionService.getAISubScriptionById(aiSubscriptionId);
            User user = userService.findUserbyUsername(username);

            if (user == null) {
                System.out.println("⚠️ 사용자를 찾을 수 없습니다: " + username);
                return;
            }


            Chat chat = Chat.builder()
                    .type(MessageType.LEAVE)
                    .sender(user)
                    .content(username + "님이 퇴장하셨습니다.")
                    .aiSubscription(aiSubscription)
                    .build();
            messagingTemplate.convertAndSend("/topic/public/" + aiSubscriptionId, chat);
        }
    }
}
