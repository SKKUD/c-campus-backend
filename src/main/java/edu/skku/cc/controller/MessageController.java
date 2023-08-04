package edu.skku.cc.controller;

import edu.skku.cc.dto.ApiResponse;
import edu.skku.cc.dto.message.CreateMessageRequestDto;
import edu.skku.cc.dto.message.MessagePublicUpdateResponseDto;
import edu.skku.cc.dto.message.MessageResponseDto;
import edu.skku.cc.dto.message.MessageSolveQuizRequestDto;
import edu.skku.cc.exception.CustomException;
import edu.skku.cc.exception.ErrorType;
import edu.skku.cc.exception.SuccessType;
import edu.skku.cc.service.MessageService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class MessageController {

    private final MessageService messageService;

    @GetMapping("/users/{userId}/messages/pulled")
    public ApiResponse<List<MessageResponseDto>> getUserPulledMessageList(@PathVariable Long userId, Authentication authentication) {
        if (authentication != null && authentication.getCredentials().equals(userId)) {
            // 수신자 본인 -> 본인의 모든 메시지 조회
            return ApiResponse.success(SuccessType.GET_USER_MESSAGE_ALL_SUCCESS, messageService.getUserPulledMessageList(userId));
        } else {
            // 방문자 -> public 메시지만 조회
            return ApiResponse.success(SuccessType.GET_USER_MESSAGE_ALL_SUCCESS, messageService.getUserPublicPulledMessageList(userId));
        }
    }

    @GetMapping("/users/{userId}/messages/{messageId}")
    public ApiResponse<MessageResponseDto> getSingleUserMessage(@PathVariable Long userId, @PathVariable Long messageId, Authentication authentication) {
        if (authentication!= null && authentication.getCredentials().equals(userId)) {
            // 수신자
            return ApiResponse.success(SuccessType.GET_USER_MESSAGE_ONE_SUCCESS, messageService.getSingleUserMessage(userId, messageId));
        } else {
            // 방문자
            return ApiResponse.success(SuccessType.GET_USER_MESSAGE_ONE_SUCCESS, messageService.getSingleUserPublicMessage(userId, messageId));
        }
    }

    @PatchMapping("/users/{userId}/messages/{messageId}")
    public ApiResponse<MessagePublicUpdateResponseDto> updateMessagePublic(@PathVariable Long userId, @PathVariable Long messageId, Authentication authentication) {
        if (!(authentication!= null && authentication.getCredentials().equals(userId))) {
            throw new CustomException(ErrorType.UNAUTHORIZED_USER_EXCEPTION);
        }
        messageService.updateMessagePublic(userId, messageId);
        return ApiResponse.success(SuccessType.UPDATE_USER_MESSAGE_PUBLIC_SUCCESS, messageService.updateMessagePublic(userId, messageId));
    }

    @GetMapping("/users/{userId}/message/remain-count")
    public ApiResponse<Long> getRemainMessageCount(@PathVariable Long userId, Authentication authentication) {
        if (!(authentication!= null && authentication.getCredentials().equals(userId))) {
            throw new CustomException(ErrorType.UNAUTHORIZED_USER_EXCEPTION);
        }
        return ApiResponse.success(SuccessType.GET_USER_REMAIN_MESSAGE_COUNT_SUCCESS, messageService.getRemainMessageCount(userId));
    }


    @GetMapping("/users/{userId}/messages/unpulled")
    public ApiResponse<Integer> getUserUnpulledMessageList(@PathVariable Long userId, Authentication authentication) {
        if (!(authentication!= null && authentication.getCredentials().equals(userId))) {
            throw new CustomException(ErrorType.UNAUTHORIZED_USER_EXCEPTION);
        }
        return ApiResponse.success(SuccessType.PULL_USER_MESSAGE_SUCCESS, messageService.pullMessage(userId));
    }

    @PostMapping("/users/{userId}/messages")
    public ApiResponse<Long> createMessage(@PathVariable Long userId, @RequestPart @Valid CreateMessageRequestDto request, @RequestPart(value = "file", required = false) MultipartFile file) {
        return ApiResponse.success(SuccessType.CREATE_MESSAGE_SUCCESS, messageService.createMessage(userId, request, file));
    }


    @PostMapping("/users/{userId}/messages/{messageId}/quiz")
    public ApiResponse solveQuiz(@PathVariable Long userId, @PathVariable Long messageId, @RequestBody @Valid MessageSolveQuizRequestDto request, Authentication authentication) {
        if (!(authentication!= null && authentication.getCredentials().equals(userId))) {
            throw new CustomException(ErrorType.UNAUTHORIZED_USER_EXCEPTION);
        }
        messageService.solveMessageQuiz(userId, messageId, request.getAnswer());
        return ApiResponse.success(SuccessType.SOLVE_MESSAGE_QUIZ_SUCCESS);
    }

    @DeleteMapping("/users/{userId}/messages/{messageId}")
    public ApiResponse deleteMessage(@PathVariable Long userId, @PathVariable Long messageId, Authentication authentication) {
        if (!(authentication!= null && authentication.getCredentials().equals(userId))) {
            throw new CustomException(ErrorType.UNAUTHORIZED_USER_EXCEPTION);
        }
        messageService.deleteMessage(userId, messageId);
        return ApiResponse.success(SuccessType.DELETE_MESSAGE_SUCCESS);
    }

    @GetMapping("/users/{userId}/photos")
    public ApiResponse<List<String>> getUserPhotoList(@PathVariable Long userId, Authentication authentication) {
        if (authentication!= null && authentication.getCredentials().equals(userId)){
            // 수신자 본인 -> 본인의 모든 사진 조회
            return ApiResponse.success(SuccessType.GET_USER_IMAGE_ALL_SUCCESS, messageService.getUserPhotoList(userId));
        }
        else {
            // 방문자 -> public 사진만 조회

        }
    }

    @PostMapping("/users/{userId}/photos")
    public ApiResponse<String> uploadUserPhoto(@PathVariable Long userId, @RequestPart MultipartFile file) {
        return ApiResponse.success(SuccessType.CREATE_USER_IMAGE_SUCCESS, messageService.uploadUserPhoto(userId, file));
    }

    @DeleteMapping("/users/{userId}/photos/{imageUuid}")
    public ApiResponse deleteUserPhoto(@PathVariable Long userId, @PathVariable String imageUuid, Authentication authentication) {
        if (!(authentication!= null && authentication.getCredentials().equals(userId))) {
            throw new CustomException(ErrorType.UNAUTHORIZED_USER_EXCEPTION);
        }
        messageService.deletePhoto(userId, imageUuid);
        return ApiResponse.success(SuccessType.DELETE_IMAGE_SUCCESS);
    }

}

