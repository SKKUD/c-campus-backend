package edu.skku.cc.controller;

import edu.skku.cc.dto.ApiResponse;
import edu.skku.cc.dto.Message.CreateMessageRequestDto;
import edu.skku.cc.dto.Message.MessagePublicUpdateResponseDto;
import edu.skku.cc.dto.Message.MessageResponseDto;
import edu.skku.cc.dto.Message.MessageSolveQuizRequestDto;
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
    public ApiResponse<List<MessageResponseDto>> getUserPulledMessageList(@PathVariable Long userId) {
        return ApiResponse.success(SuccessType.GET_USER_MESSAGE_ALL_SUCCESS, messageService.getUserPulledMessageList(userId));
    }

    @PreAuthorize("@webSecurity.checkAuthority(authentication, #userId)")
    @GetMapping("/users/{userId}/messages/{messageId}")
    public ApiResponse<MessageResponseDto> getSingleUserMessage(@PathVariable Long userId, @PathVariable Long messageId, Authentication authentication) {
        return ApiResponse.success(SuccessType.GET_USER_MESSAGE_ONE_SUCCESS, messageService.getSingleUserMessage(userId, messageId));
    }

    @PreAuthorize("@webSecurity.checkAuthority(authentication, #userId)")
    @PatchMapping("/users/{userId}/messages/{messageId}")
    public ApiResponse<MessagePublicUpdateResponseDto> updateMessagePublic(@PathVariable Long userId, @PathVariable Long messageId) {
        messageService.updateMessagePublic(userId, messageId);
        return ApiResponse.success(SuccessType.UPDATE_USER_MESSAGE_PUBLIC_SUCCESS, messageService.updateMessagePublic(userId, messageId));
    }

    @GetMapping("/users/{userId}/message/remain-count")
    public ApiResponse<Long> getRemainMessageCount(@PathVariable Long userId) {
        return ApiResponse.success(SuccessType.GET_USER_REMAIN_MESSAGE_COUNT_SUCCESS, messageService.getRemainMessageCount(userId));
    }

    @PreAuthorize("@webSecurity.checkAuthority(authentication, #userId)")
    @GetMapping("/users/{userId}/messages/unpulled")
    public ApiResponse<Integer> getUserUnpulledMessageList(@PathVariable Long userId) {
        return ApiResponse.success(SuccessType.PULL_USER_MESSAGE_SUCCESS, messageService.pullMessage(userId));
    }

    @PostMapping("/users/{userId}/messages")
    public ApiResponse<Long> createMessage(@PathVariable Long userId, @RequestPart @Valid CreateMessageRequestDto request, @RequestPart(value = "file", required = false) MultipartFile file) {
        return ApiResponse.success(SuccessType.CREATE_MESSAGE_SUCCESS, messageService.createMessage(userId, request, file));
    }

    @PostMapping("/users/{userId}/messages/{messageId}/quiz")
    public ApiResponse solveQuiz(@PathVariable Long userId, @PathVariable Long messageId, @RequestBody @Valid MessageSolveQuizRequestDto request) {
        messageService.solveMessageQuiz(userId, messageId, request.getAnswer());
        return ApiResponse.success(SuccessType.SOLVE_MESSAGE_QUIZ_SUCCESS);
    }

    @DeleteMapping("/users/{userId}/messages/{messageId}")
    public ApiResponse deleteMessage(@PathVariable Long userId, @PathVariable Long messageId) {
        messageService.deleteMessage(userId, messageId);
        return ApiResponse.success(SuccessType.DELETE_MESSAGE_SUCCESS);
    }
}

