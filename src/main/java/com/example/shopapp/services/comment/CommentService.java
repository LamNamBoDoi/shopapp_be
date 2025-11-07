package com.example.shopapp.services.comment;

import com.example.shopapp.components.TranslateMessages;
import com.example.shopapp.dtos.CommentDTO;
import com.example.shopapp.exceptions.DataNotFoundException;
import com.example.shopapp.models.Comment;
import com.example.shopapp.models.Product;
import com.example.shopapp.models.User;
import com.example.shopapp.repositories.CommentRepository;
import com.example.shopapp.repositories.ProductRepository;
import com.example.shopapp.repositories.UserRepository;
import com.example.shopapp.response.CommentResponse;
import com.example.shopapp.utils.MessageKeys;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CommentService extends TranslateMessages implements ICommentService{
    private final CommentRepository commentRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;

    @Transactional
    @Override
    public Comment insertComment(CommentDTO commentDTO) throws DataNotFoundException {
        User user = userRepository.findById(commentDTO.getUserId()).orElse(null);
        Product product = productRepository.findById(commentDTO.getProductId()).orElse(null);

        if(user == null){
            throw new DataNotFoundException(translate(MessageKeys.USER_NOT_FOUND));
        }
        if(product == null){
            throw new DataNotFoundException(translate(MessageKeys.PRODUCT_NOT_FOUND));
        }
        Comment newComment = Comment.builder()
                .user(user)
                .product(product)
                .content(commentDTO.getContent())
                .build();

        return commentRepository.save(newComment);
    }

    @Transactional
    @Override
    public void deleteComment(Long id) {
        commentRepository.deleteById(id);
    }

    @Transactional
    @Override
    public void updateComment(Long id, CommentDTO commentDTO) throws DataNotFoundException {
        Comment existsComment = commentRepository.findById(id)
                .orElseThrow(() -> new DataNotFoundException(translate(MessageKeys.COMMENT_NOT_FOUND, id)));

        existsComment.setContent(commentDTO.getContent());
        commentRepository.save(existsComment);
    }

    @Override
    public List<CommentResponse> getCommentByUserAndProduct(Long userId, Long productId) {
        List<Comment> comments = commentRepository.findByUserIdAndProductId(userId, productId);
        return comments.stream().map(CommentResponse::fromComment).toList();
    }

    @Override
    public List<CommentResponse> getCommentByProduct(Long productId) {
        List<Comment> comments = commentRepository.findByProductId(productId);
        return comments.stream().map(CommentResponse::fromComment).toList();
    }

}
