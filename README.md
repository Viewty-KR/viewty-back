# viewty-back

Review CRUD endpoints:

- `GET /api/reviews?productId={productId}` (pageable)
- `POST /api/reviews?productId={productId}`
- `PUT /api/reviews/{reviewId}`
- `DELETE /api/reviews/{reviewId}`

Request bodies:

- `ReviewCreateRequest`: `{ "name": "...", "content": "...", "rating": 1-5 }`
- `ReviewUpdateRequest`: `{ "content": "...", "rating": 1-5 }`

Comment endpoints:

- `GET /api/comments?reviewId={reviewId}` (pageable)
- `GET /api/comments/me?userId={userId}` (pageable)
- `POST /api/comments?reviewId={reviewId}&userId={userId}`
- `PUT /api/comments/{commentId}?userId={userId}`
- `DELETE /api/comments/{commentId}?userId={userId}`

Comment request bodies:

- `CommentCreateRequest`: `{ "content": "..." }`
- `CommentUpdateRequest`: `{ "content": "..." }`

Bookmark endpoint:

- `POST /api/bookmarks/toggle?userId={userId}&productId={productId}`
- `GET /api/bookmarks?userId={userId}`
- `GET /api/bookmarks/status?userId={userId}&productId={productId}`
