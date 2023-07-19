package edu.skku.cc.dto.Message;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class MessagePublicUpdateResponseDto {
    private Long messageId; // update된 메시지 id
    private Boolean isPublic; // update된 공개 여부
}
