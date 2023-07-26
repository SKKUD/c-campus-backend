package edu.skku.cc.service;

import edu.skku.cc.domain.Message;
import edu.skku.cc.domain.Quiz;
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

    /**
     * User 메시지 중 pull 되지 않은 것 몇 개인지 체크
     */
    public Long getRemainMessageCount(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorType.INVALID_USER));
        List<Message> messageList = user.getMessages();
        return messageList.stream()
                .filter(message -> !message.getIsPulled())
                .count();
    }

    /**
     * User 메시지 중 pull 되지 않은 것 5개 단위로 pull
     */
    @Transactional
    public Integer pullMessage(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorType.INVALID_USER));
        List<Message> messageList = user.getMessages();

        // 5개 미만인 경우
        if (messageList.stream().filter(message -> !message.getIsPulled()).count() < 5) {
            throw new CustomException(ErrorType.INVALID_PULL_REQUEST_EXCEPTION);
        } else { // 5개 이상인 경우 5개 단위로 pull
            List<Message> unpulledMessageList = messageList.stream()
                    .filter(message -> !message.getIsPulled())
                    .sorted((m1, m2) -> m1.getCreatedAt().compareTo(m2.getCreatedAt()))
                    .toList();

            int len = unpulledMessageList.size();
            int pullCount = len / 5;

            for (int i = 0; i < pullCount; i++) {
                for (int j = 0; j < 5; j++) {
                    unpulledMessageList.get(i * 5 + j).pullMessage();
                }
            }
            return pullCount * 5;
        }
    }

    public void solveMessageQuiz(Long userId, Long messageId, String answer) {

        // 메시지 수신인인지 체크
        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new CustomException(ErrorType.INVALID_MESSAGE));
        Quiz messageQuiz = message.getQuiz();
        if (messageQuiz.getIsSolved()) {
            throw new CustomException(ErrorType.INVALID_SOLVE_REQUEST_EXCEPTION);
        }
        if (messageQuiz.getAnswer().equals(answer)) {
            // 이게 맞는지 아니면 messageQuiz.solveQuiz()로 해야하는지 모르겠음
            message.solveQuiz();
        } else {
            throw new CustomException(ErrorType.WRONG_ANSWER_EXCEPTION);
        }
    }

    public Long deleteMessage(Long userId, Long messageId) {
        // 권한 체크
        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new CustomException(ErrorType.INVALID_MESSAGE));
        messageRepository.delete(message);
        return messageId;
    }


}
