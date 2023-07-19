package edu.skku.cc.service;

import edu.skku.cc.domain.Message;
import edu.skku.cc.domain.User;
import edu.skku.cc.dto.Message.MessageListResponseDto;
import edu.skku.cc.dto.Message.MessagePublicUpdateResponseDto;
import edu.skku.cc.dto.Message.SingleMessageResponseDto;
import edu.skku.cc.exception.CustomException;
import edu.skku.cc.exception.ErrorType;
import edu.skku.cc.repository.MessageRepository;
import edu.skku.cc.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MessageService {

    private final MessageRepository messageRepository;
    private final UserRepository userRepository;

    /**
     * Get all messages that user pulled
     * Order by pulledAt DESC
     */
    public List<MessageListResponseDto> getUserPulledMessageList(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorType.INVALID_USER));
        List<Message> messageList = user.getMessages();

        return messageList.stream()
                .filter(Message::getIsPulled)
                .sorted((m1, m2) -> m2.getPulledAt().compareTo(m1.getPulledAt()))
                .map(MessageListResponseDto::of)
                .collect(Collectors.toList());
    }

    public SingleMessageResponseDto getSingleUserMessage(Long userId, Long messageId) {
//        User user = userRepository.getUserById(userId);
        // 수신인이면 open 여부 체크하기
        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new CustomException(ErrorType.INVALID_MESSAGE));
        return SingleMessageResponseDto.of(message);
    }

    @Transactional
    public MessagePublicUpdateResponseDto updateMessagePublic(Long userId, Long messageId) {
        // 메시지 수신인인지 체크
        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new CustomException(ErrorType.INVALID_MESSAGE));
        message.updateIsPublic();
        return MessagePublicUpdateResponseDto.builder()
                .messageId(message.getId())
                .isPublic(message.getIsPublic())
                .build();
    }
}
