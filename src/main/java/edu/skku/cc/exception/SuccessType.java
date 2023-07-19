package edu.skku.cc.exception;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor(access =  AccessLevel.PRIVATE)
public enum SuccessType {

    /**
     * 200 OK
     */
    GET_USER_MESSAGE_ALL_SUCCESS(HttpStatus.OK,"유저 메시지 전체 조회에 성공했습니다."),
    GET_USER_MESSAGE_ONE_SUCCESS(HttpStatus.OK,"메시지 조회에 성공했습니다."),
    USER_MESSAGE_ALL_SUCCESS(HttpStatus.OK,"메시지 공개 여부 변경에 성공했습니다."),
    PULL_USER_MESSAGE_SUCCESS(HttpStatus.OK,"메시지 뽑기에 성공했습니다."),
    SOLVE_MESSAGE_QUIZ_SUCCESS(HttpStatus.OK,"메시지 퀴즈 풀기에 성공했습니다."),
    GET_USER_IMAGE_ALL_SUCCESS(HttpStatus.OK,"유저 이미지 전체 조회에 성공했습니다."),


    /**
     * 201 CREATED
     */
    CREATE_USER_MESSAGE_SUCCESS(HttpStatus.CREATED,"메시지 생성에 성공했습니다."),
    CREATE_USER_IMAGE_SUCCESS(HttpStatus.OK,"이미지 업로드에 성공했습니다.");



    private final HttpStatus httpStatus;
    private final String message;

    public int getHttpStatusCode() {
        return httpStatus.value();
    }
}
