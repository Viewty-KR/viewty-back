package com.viewty.viewtyback.dto.response;

import com.viewty.viewtyback.entity.ProductBookMark;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class BookmarkResponse {
    private Boolean bookmarked;
    private Long bookmarkId;
    private Long productId;
    private String productName;
    private String productImgUrl;
    private LocalDateTime createdAt;

    public static BookmarkResponse fromStatus(boolean bookmarked) {
        return BookmarkResponse.builder()
                .bookmarked(bookmarked)
                .build();
    }

    public static BookmarkResponse from(ProductBookMark bookmark) {
        return BookmarkResponse.builder()
                .bookmarkId(bookmark.getId())
                .productId(bookmark.getProduct().getId())
                .productName(bookmark.getProduct().getName())
                .productImgUrl(bookmark.getProduct().getImgUrl())
                .createdAt(bookmark.getCreatedAt())
                .build();
    }
}
