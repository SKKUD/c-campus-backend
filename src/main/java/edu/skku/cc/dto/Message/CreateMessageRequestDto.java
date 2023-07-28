package edu.skku.cc.dto.Message;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.ToString;
import org.hibernate.validator.constraints.Length;

@Getter
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@ToString
public class CreateMessageRequestDto {
    @NotEmpty
    private String category;

    @NotEmpty
    private String content;

    @NotEmpty
    @Length(max = 10)
    private String author;

    @NotEmpty
    private String backgroundColorCode;

    @NotNull
    private Boolean isQuiz;

    private String quizContent;

    private String quizAnswer;
}
