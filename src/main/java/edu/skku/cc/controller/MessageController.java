package edu.skku.cc.controller;

import edu.skku.cc.dto.ApiResponse;
import edu.skku.cc.dto.Message.MessageListResponseDto;
import edu.skku.cc.dto.Message.MessagePublicUpdateResponseDto;
import edu.skku.cc.dto.Message.SingleMessageResponseDto;
import edu.skku.cc.exception.SuccessType;
import edu.skku.cc.service.MessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;


import java.util.List;

@RestController
@RequiredArgsConstructor
public class MessageController {

    private MessageService messageService;

    @GetMapping("/users/{userId}/messages/pulled")
    public ApiResponse<List<MessageListResponseDto>> getUserPulledMessageList(@PathVariable Long userId) {
        return ApiResponse.success(SuccessType.GET_USER_MESSAGE_ALL_SUCCESS, messageService.getUserPulledMessageList(userId));
    }

    @GetMapping("/users/{userId}/messages/{messageId}")
    public ApiResponse<SingleMessageResponseDto> getSingleUserMessage(@PathVariable Long userId, @PathVariable Long messageId) {
        return ApiResponse.success(SuccessType.GET_USER_MESSAGE_ONE_SUCCESS, messageService.getSingleUserMessage(userId, messageId));
    }

    @PatchMapping("/users/{userId}/messages/{messageId}")
    public ApiResponse<MessagePublicUpdateResponseDto> updateMessagePublic(@PathVariable Long userId, @PathVariable Long messageId) {
        messageService.updateMessagePublic(userId, messageId);
        return ApiResponse.success(SuccessType.UPDATE_USER_MESSAGE_PUBLIC_SUCCESS, messageService.updateMessagePublic(userId, messageId));
    }

    @GetMapping("/users/{userId}/message/remain-count")
    public ApiResponse<Long> getRemainMessageCount(@PathVariable Long userId) {
        return ApiResponse.success(SuccessType.GET_USER_REMAIN_MESSAGE_COUNT_SUCCESS, messageService.getRemainMessageCount(userId));
    }

    @GetMapping("/users/{userId}/messages/unpulled")
    public ApiResponse<Integer> getUserUnpulledMessageList(@PathVariable Long userId) {
        return ApiResponse.success(SuccessType.PULL_USER_MESSAGE_SUCCESS, messageService.pullMessage(userId));
    }

}

