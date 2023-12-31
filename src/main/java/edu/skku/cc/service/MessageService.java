package edu.skku.cc.service;

import edu.skku.cc.domain.*;
import edu.skku.cc.dto.message.CreateMessageRequestDto;
import edu.skku.cc.dto.message.MessagePublicUpdateResponseDto;
import edu.skku.cc.dto.message.MessageResponseDto;
import edu.skku.cc.exception.CustomException;
import edu.skku.cc.exception.ErrorType;
import edu.skku.cc.repository.MessageRepository;
import edu.skku.cc.repository.PhotoRepository;
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
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MessageService {

    private final MessageRepository messageRepository;
    private final UserRepository userRepository;
    private final PhotoRepository photoRepository;

    private final S3Client s3Client;

    @Value("${aws.s3.bucket}")
    private String BUCKET_NAME;

    @Value("${aws.s3.region}")
    private String REGION;

    /**
     * Get all messages that user pulled
     * Order by pulledAt DESC
     */
    public List<MessageResponseDto> getUserPulledMessageList(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorType.INVALID_USER_EXCEPTION));
        List<Message> messageList = user.getMessages();

        return messageList.stream()
                .filter(Message::getIsPulled)
                .sorted(Comparator.comparing(Message::getPulledAt).reversed())
                .map(MessageResponseDto::of)
                .peek(dto -> {
                    dto.setImageUrl(getUrl(dto.getImageUuid()));
                })
                .collect(Collectors.toList());
    }

    public List<MessageResponseDto> getUserPublicPulledMessageList(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorType.INVALID_USER_EXCEPTION));
        List<Message> messageList = user.getMessages();

        return messageList.stream()
                .filter(message -> message.getIsPulled() && message.getIsPublic())
                .sorted(Comparator.comparing(Message::getPulledAt).reversed())
                .map(MessageResponseDto::of)
                .peek(dto -> {
                    dto.setImageUrl(getUrl(dto.getImageUuid()));
                })
                .collect(Collectors.toList());
    }

    @Transactional
    public MessageResponseDto getSingleUserMessage(UUID userId, Long messageId) {
        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new CustomException(ErrorType.INVALID_MESSAGE_EXCEPTION));
        // open 여부 체크
        if (!message.getIsOpened()) {
            message.openMessage();
        }

        MessageResponseDto dto = MessageResponseDto.of(message);
        dto.setImageUrl(getUrl(dto.getImageUuid()));
        return dto;
    }

    public MessageResponseDto getSingleUserPublicMessage(UUID userId, Long messageId) {
        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new CustomException(ErrorType.INVALID_MESSAGE_EXCEPTION));

        if (!message.getIsPublic()) {
            throw new CustomException(ErrorType.UNAUTHORIZED_USER_EXCEPTION);
        }
        MessageResponseDto dto = MessageResponseDto.of(message);
        dto.setImageUrl(getUrl(dto.getImageUuid()));
        return dto;
    }

    @Transactional
    public MessagePublicUpdateResponseDto updateMessagePublic(UUID userId, Long messageId) {
        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new CustomException(ErrorType.INVALID_MESSAGE_EXCEPTION));
        message.updateIsPublic();
        return MessagePublicUpdateResponseDto.builder()
                .messageId(message.getId())
                .isPublic(message.getIsPublic())
                .build();
    }

    /**
     * User 메시지 중 pull 되지 않은 것 몇 개인지 체크
     */
    public Long getRemainMessageCount(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorType.INVALID_USER_EXCEPTION));
        List<Message> messageList = user.getMessages();
        return messageList.stream()
                .filter(message -> !message.getIsPulled())
                .count();
    }

    /**
     * User 메시지 중 pull 되지 않은 것 5개 단위로 pull
     */
    @Transactional
    public Integer pullMessage(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorType.INVALID_USER_EXCEPTION));
        List<Message> messageList = user.getMessages();

        // 5개 미만인 경우
        if (messageList.stream().filter(message -> !message.getIsPulled()).count() < 5) {
            throw new CustomException(ErrorType.INVALID_PULL_REQUEST_EXCEPTION);
        } else { // 5개 이상인 경우 가장 먼저 들어온 5개 pull
            List<Message> unpulledMessageList = messageList.stream()
                    .filter(message -> !message.getIsPulled())
                    .sorted(Comparator.comparing(BaseTimeEntity::getCreatedAt))
                    .toList();

            for (int i = 0; i < 5; i++) {
                unpulledMessageList.get(i).pullMessage();
            }
            return 5;
        }
    }

    @Transactional
    public void solveMessageQuiz(UUID userId, Long messageId, String answer) {
        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new CustomException(ErrorType.INVALID_MESSAGE_EXCEPTION));
        Quiz messageQuiz = message.getQuiz();
        if (messageQuiz.getIsSolved()) {
            throw new CustomException(ErrorType.INVALID_SOLVE_REQUEST_EXCEPTION);
        }
        if (messageQuiz.getAnswer().equals(answer)) {
            message.solveQuiz();
        } else {
            throw new CustomException(ErrorType.WRONG_ANSWER_EXCEPTION);
        }
    }

    @Transactional
    public void deleteMessage(UUID userId, Long messageId) {
        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new CustomException(ErrorType.INVALID_MESSAGE_EXCEPTION));

        if (!Objects.equals(message.getUser().getId(), userId)) {
            throw new CustomException(ErrorType.INVALID_USER_EXCEPTION);
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
    public Long createMessage(UUID userId, CreateMessageRequestDto request, MultipartFile file) {
        if (file != null && !file.getContentType().startsWith("image")) {
            throw new CustomException(ErrorType.INVALID_FILE_TYPE_EXCEPTION);
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorType.INVALID_USER_EXCEPTION));

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

        if (file != null) {
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

    @Transactional
    public String uploadUserPhoto(UUID userId, MultipartFile file) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorType.INVALID_USER_EXCEPTION));

        if (file.getContentType() != null && !file.getContentType().startsWith("image")) {
            throw new CustomException(ErrorType.INVALID_FILE_TYPE_EXCEPTION);
        }

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

        Photo photo = Photo.builder()
                .imageUuid(uuid)
                .build();

        user.addPhoto(photo);
        userRepository.save(user);

        return getUrl(uuid);
    }

    @Transactional
    public void deletePhoto(UUID userId, String imageUuid) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorType.INVALID_USER_EXCEPTION));

        Photo photo = photoRepository.findByImageUuid(UUID.fromString(imageUuid))
                .orElseThrow(() -> new CustomException(ErrorType.INVALID_IMAGE_EXCEPTION));

        if (!Objects.equals(photo.getUser().getId(), user.getId())) {
            throw new CustomException(ErrorType.UNAUTHORIZED_USER_EXCEPTION);
        }

        UUID uuid = photo.getImageUuid();
        s3Client.deleteObject(DeleteObjectRequest.builder()
                .bucket(BUCKET_NAME)
                .key(uuid.toString())
                .build());

        photoRepository.delete(photo);
    }

    /**
     * 1) Message 없는 경우
     * 2) Message pull 되었고 quiz 없는 경우
     * 3) Message pull 되었고 quiz 푼 경우
     */
    public List<String> getUserPhotoList(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorType.INVALID_USER_EXCEPTION));

        List<Photo> photoList = user.getPhotos();

        return photoList.stream()

                .filter(photo -> (photo.getMessage() == null ||
                        (photo.getMessage().getIsPulled() && (photo.getMessage().getQuiz() == null || photo.getMessage().getQuiz().getIsSolved()))))
                .map(eachPhoto -> getUrl(eachPhoto.getImageUuid()))
                .toList();
    }

    private String getUrl(UUID uuid) {
        if (uuid == null) {
            return null;
        }
        return String.format("https://%s.s3.%s.amazonaws.com/%s", BUCKET_NAME, REGION, uuid);
    }
}
