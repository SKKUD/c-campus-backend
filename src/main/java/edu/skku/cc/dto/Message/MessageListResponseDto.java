package edu.skku.cc.dto.Message;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import edu.skku.cc.domain.Message;
import edu.skku.cc.domain.Quiz;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class MessageListResponseDto {

    private Long messageId; // 메시지 id
    private Long userId;
    private String category;
    private String content;
    private String author;
    private Boolean isOpened;
    private Boolean isPulled;
    private LocalDateTime pulledAt;
    //    private Photo photo;
    private String imageUrl;
    private String backgroundColorCode;
    private Boolean isPublic;

    private Quiz quiz;

    public static MessageListResponseDto of(Message message) {
        return MessageListResponseDto.builder()
                .messageId(message.getId())
                .userId(message.getUser().getId())
                .category(message.getCategory())
                .content(message.getContent())
                .author(message.getAuthor())
                .isOpened(message.getIsOpened())
                .isPulled(message.getIsPulled())
                .pulledAt(message.getPulledAt())
                .imageUrl(message.getPhoto().getImageUrl())
                .backgroundColorCode(message.getBackgroundColorCode())
                .isPublic(message.getIsPublic())
                .quiz(message.getQuiz())
                .build();
    }
}
