package com.example.demo.comment;

import com.example.demo.board.Board;
import com.example.demo.board.BoardRepository;
import com.example.demo.user.User;
import com.example.demo.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.hibernate.Hibernate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

@Transactional(readOnly = true)
@RequiredArgsConstructor
@Service
public class CommentService {
    private final CommentRepository commentRepository;
    private final BoardRepository boardRepository;
    private final UserRepository userRepository;

    @Transactional
    public Comment save(CommentDto commentDto) {
        Optional<User> optionalUser = userRepository.findById(commentDto.getUserId());
        Optional<Board> optionalBoard = boardRepository.findById(commentDto.getBoardId());
        if (optionalUser.isPresent() && optionalBoard.isPresent()){
            User user = optionalUser.get();
            Board board = optionalBoard.get();
            Hibernate.initialize(user.getComments());
            Hibernate.initialize(user.getBoards());
            Hibernate.initialize(board.getComments());
            Hibernate.initialize(board.getBoardFiles());

            Comment comment = Comment.builder()
                    .contents(commentDto.getContents())
                    .createTime(LocalDateTime.now())
                    .updateTime(LocalDateTime.now())
                    .board(board)
                    .user(user)
                    .build();
            commentRepository.save(comment);
            return comment;
        } else {
            return null;
        }
    }

    public List<CommentDto> commentList(Long id) {
        List<Comment> comments = commentRepository.findByBoard_id(id);
        List<CommentDto> commentDtos = new LinkedList<>();
        for (Comment c : comments){
            commentDtos.add(CommentDto.toCommentDto(c));
        }
        return commentDtos;
    }

    @Transactional
    public void delete(Long id) {
        commentRepository.deleteById(id);
    }

    @Transactional
    public Comment update(CommentDto commentDto) {
        Optional<Comment> commentOptional = commentRepository.findById(commentDto.getId());
        if (commentOptional.isPresent()){
            Comment comment = commentOptional.get();
            comment.updateFromDto(commentDto);
            return comment;
        } else {
            return null;
        }
    }
}
