package com.example.shopapp.services.comment;

import com.example.shopapp.dtos.CommentDTO;
import com.example.shopapp.exceptions.DataNotFoundException;
import com.example.shopapp.models.Comment;
import com.example.shopapp.response.CommentResponse;

import java.util.List;

public interface ICommentService {
    Comment insertComment(CommentDTO commentDTO) throws DataNotFoundException;
    void deleteComment(Long id);
    void updateComment(Long id, CommentDTO comment) throws DataNotFoundException;
    List<CommentResponse> getCommentByUserAndProduct(Long userId, Long productId);
    List<CommentResponse> getCommentByProduct(Long productId);

}
