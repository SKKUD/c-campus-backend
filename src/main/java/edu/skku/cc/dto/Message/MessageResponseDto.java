package edu.skku.cc.dto.Message;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import edu.skku.cc.domain.Message;
import edu.skku.cc.domain.Quiz;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class MessageResponseDto {

    private Long messageId; // 메시지 id
    private Long userId;
    private String category;
    private String content;
    private String author;
    private Boolean isOpened;
    private Boolean isPulled;
    private LocalDateTime pulledAt;
    private UUID imageUuid;
    private String backgroundColorCode;
    private Boolean isPublic;

    private Quiz quiz;

    public static MessageResponseDto of(Message message) {
        return MessageResponseDto.builder()
                .messageId(message.getId())
                .userId(message.getUser().getId())
                .category(message.getCategory())
                .content(message.getContent())
                .author(message.getAuthor())
                .isOpened(message.getIsOpened())
                .isPulled(message.getIsPulled())
                .pulledAt(message.getPulledAt())
                // Check if photo is null before calling getImageUrl
                .imageUuid(message.getImageUuid())
                .backgroundColorCode(message.getBackgroundColorCode())
                .isPublic(message.getIsPublic())
                .quiz(message.getQuiz())
                .build();
    }
}
