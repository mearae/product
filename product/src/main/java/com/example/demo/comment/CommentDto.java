package com.example.demo.comment;

import lombok.*;

import java.time.LocalDateTime;

@Setter
@Getter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class CommentDto {

    private Long id;

    private String writer;

    private String contents;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    private Long boardId;

    private Long userId;

    public Comment toEntity(){
        return Comment.builder()
                .id(id)
                .contents(contents)
                .createTime(createTime)
                .updateTime(LocalDateTime.now())
                .build();
    }

    public static CommentDto toCommentDto(Comment comment){
        return new CommentDto(
                comment.getId(),
                comment.getUser().getName(),
                comment.getContents(),
                comment.getCreateTime(),
                comment.getUpdateTime(),
                comment.getBoard().getId(),
                comment.getUser().getId());
    }
}
