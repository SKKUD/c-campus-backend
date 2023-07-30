package edu.skku.cc.service;

import edu.skku.cc.domain.Message;
import edu.skku.cc.domain.Photo;
import edu.skku.cc.domain.Quiz;
import edu.skku.cc.domain.User;
import edu.skku.cc.dto.Message.CreateMessageRequestDto;
import edu.skku.cc.dto.Message.MessagePublicUpdateResponseDto;
import edu.skku.cc.dto.Message.MessageResponseDto;
import edu.skku.cc.exception.CustomException;
import edu.skku.cc.exception.ErrorType;
import edu.skku.cc.repository.MessageRepository;
import edu.skku.cc.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MessageService {

    private final MessageRepository messageRepository;
    private final UserRepository userRepository;

    private final S3Client s3Client;

    @Value("${aws.s3.bucket}")
    private String BUCKET_NAME;

    /**
     * Get all messages that user pulled
     * Order by pulledAt DESC
     */
    public List<MessageResponseDto> getUserPulledMessageList(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorType.INVALID_USER));
        List<Message> messageList = user.getMessages();

        return messageList.stream()
                .filter(Message::getIsPulled)
                .sorted((m1, m2) -> m2.getPulledAt().compareTo(m1.getPulledAt()))
                .map(MessageResponseDto::of)
                .collect(Collectors.toList());
    }

    public MessageResponseDto getSingleUserMessage(Long userId, Long messageId) {
//        User user = userRepository.getUserById(userId);
        // 수신인이면 open 여부 체크하기
        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new CustomException(ErrorType.INVALID_MESSAGE));
        return MessageResponseDto.of(message);
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

    @Transactional
    public void deleteMessage(Long userId, Long messageId) {
        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new CustomException(ErrorType.INVALID_MESSAGE));

        if (message.getUser().getId() != userId) {
            throw new CustomException(ErrorType.INVALID_USER);
        }

        Photo photo = message.getPhoto();
        if (photo != null) {
            UUID uuid = photo.getImageUuid();
            s3Client.deleteObject(DeleteObjectRequest.builder()
                    .bucket(BUCKET_NAME)
                    .key(uuid.toString())
                    .build());
        }

        messageRepository.delete(message);
    }


    @Transactional
    public Long createMessage(Long userId, CreateMessageRequestDto request, MultipartFile file) {
        log.info("userId: {}", userId);
        log.info("request: {}", request.toString());
        log.info("file: {}, type: {}", file.getOriginalFilename(), file.getContentType());


        if (file.getContentType() != null && !file.getContentType().startsWith("image")) {
            throw new CustomException(ErrorType.INVALID_FILE_TYPE_EXCEPTION);
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorType.INVALID_USER));

        Message message = Message.builder()
                .user(user)
                .category(request.getCategory())
                .content(request.getContent())
                .author(request.getAuthor())
                .backgroundColorCode(request.getBackgroundColorCode())
                .isOpened(false)
                .isPulled(false)
                .isPublic(false)
                .build();

        if (!file.isEmpty()) {
            UUID uuid = UUID.randomUUID(); // UUID for s3 file name

            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(BUCKET_NAME)
                    .key(uuid.toString())
                    .contentType(file.getContentType())
                    .build();

            try {
                s3Client.putObject(putObjectRequest, RequestBody.fromBytes(file.getBytes()));
            } catch (IOException e) {
                throw new CustomException(ErrorType.FILE_UPLOAD_EXCEPTION);
            }

            message.setPhoto(uuid);
        }

        if (request.getIsQuiz()) {
            message.setQuiz(request.getQuizContent(), request.getQuizAnswer());
        }

        messageRepository.save(message);

        // TODO: DB 오류 발생시 S3에 저장된 파일 삭제

        return message.getId();
    }
}
