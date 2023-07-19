package edu.skku.cc.exception;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public enum ErrorType {

    /**
     * 400 BAD REQUEST
     */
    REQUEST_VALIDATION_EXCEPTION(HttpStatus.BAD_REQUEST, "잘못된 요청입니다"),
    INVALID_PULL_REQUEST_EXCEPTION(HttpStatus.BAD_REQUEST, "뽑지 않은 쪽지가 5개 미만입니다"),



    /**
     * 401 UNAUTHORIZED
     */
    // INVALID_USER(HttpStatus.UNAUTHORIZED, "접근 권한이 없는 유저입니다."),




    /**
     * 404 NOT FOUND
     */
    INVALID_USER(HttpStatus.NOT_FOUND, "존재하지 않는 유저입니다."),
    INVALID_MESSAGE(HttpStatus.NOT_FOUND, "존재하지 않는 메시지입니다.");




    private final HttpStatus httpStatus;
    private final String message;

    public int getHttpStatusCode() {
        return httpStatus.value();
    }
}
