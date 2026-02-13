package com.viewty.viewtyback.controller;

import com.viewty.viewtyback.dto.response.ApiResponse;
import com.viewty.viewtyback.dto.response.BookmarkResponse;
import com.viewty.viewtyback.service.BookMarkService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.viewty.viewtyback.security.CustomUserDetails;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

import java.util.List;

@RestController
@RequestMapping("/api/bookmarks")
@RequiredArgsConstructor
public class BookMarkController {

    private final BookMarkService bookMarkService;

    @PostMapping("/toggle")
    public ApiResponse<BookmarkResponse> toggle(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam Long productId
    ) {
        Long userId = userDetails.getId();
        return ApiResponse.success(bookMarkService.toggle(userId, productId));
    }

    @GetMapping
    public ApiResponse<List<BookmarkResponse>> getMyBookmarks(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        Long userId = userDetails.getId();
        return ApiResponse.success(bookMarkService.getMyBookmarks(userId));
    }

    @GetMapping("/status")
    public ApiResponse<BookmarkResponse> getBookmarkStatus(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam Long productId
    ) {
        Long userId = (userDetails == null) ? null : userDetails.getId();
        return ApiResponse.success(bookMarkService.getBookmarkStatus(userId, productId));
    }
}
