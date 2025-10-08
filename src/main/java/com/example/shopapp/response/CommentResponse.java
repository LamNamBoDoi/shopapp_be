package com.example.shopapp.response;

import com.example.shopapp.models.Comment;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CommentResponse {
    @JsonProperty("content")
    private String content;

    @JsonProperty("user")
    private UserResponse userResponse;

    @JsonProperty("product")
    private ProductLiteResponse productResponse;

    @JsonProperty("updated_at")
    private LocalDateTime updatedAt;

    public static CommentResponse fromComment(Comment comment) {
        return CommentResponse.builder()
                .content(comment.getContent())
                .userResponse(UserResponse.fromUser(comment.getUser()))
                .productResponse(ProductLiteResponse.fromProduct(comment.getProduct()))
                .updatedAt(comment.getUpdatedAt())
                .build();
    }

    public static List<CommentResponse> fromCommentList(List<Comment> comments) {
        if (comments == null || comments.isEmpty()) {
            return new ArrayList<>();
        }

        return comments.stream()
                .map(CommentResponse::fromComment)
                .collect(Collectors.toList());
    }

}
