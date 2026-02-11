package com.viewty.viewtyback.service;

import com.viewty.viewtyback.dto.response.BookmarkResponse;
import com.viewty.viewtyback.entity.Product;
import com.viewty.viewtyback.entity.ProductBookMark;
import com.viewty.viewtyback.entity.User;
import com.viewty.viewtyback.exception.CustomException;
import com.viewty.viewtyback.exception.ErrorCode;
import com.viewty.viewtyback.repository.ProductBookMarkRepository;
import com.viewty.viewtyback.repository.ProductRepository;
import com.viewty.viewtyback.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BookMarkService {

    private final ProductBookMarkRepository productBookMarkRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;

    @Transactional
    public BookmarkResponse toggle(Long userId, Long productId) {
        validateUserExists(userId);
        validateProductExists(productId);

        boolean exists = productBookMarkRepository.existsByUserIdAndProductId(userId, productId);
        if (exists) {
            productBookMarkRepository.deleteByUserIdAndProductId(userId, productId);
            return BookmarkResponse.fromStatus(false);
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new CustomException(ErrorCode.PRODUCT_NOT_FOUND));
        productBookMarkRepository.save(ProductBookMark.builder()
                .user(user)
                .product(product)
                .build());

        return BookmarkResponse.fromStatus(true);
    }

    public List<BookmarkResponse> getMyBookmarks(Long userId) {
        validateUserExists(userId);
        return productBookMarkRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(BookmarkResponse::from)
                .collect(Collectors.toList());
    }

    public BookmarkResponse getBookmarkStatus(Long userId, Long productId) {
        validateProductExists(productId);
        if (userId == null) {
            return BookmarkResponse.fromStatus(false);
        }
        validateUserExists(userId);
        boolean exists = productBookMarkRepository.existsByUserIdAndProductId(userId, productId);
        return BookmarkResponse.fromStatus(exists);
    }

    private void validateUserExists(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new CustomException(ErrorCode.USER_NOT_FOUND);
        }
    }

    private void validateProductExists(Long productId) {
        if (!productRepository.existsById(productId)) {
            throw new CustomException(ErrorCode.PRODUCT_NOT_FOUND);
        }
    }
}
