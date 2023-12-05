package com.example.demo.board;

import lombok.*;

import java.time.LocalDateTime;

@Setter
@Getter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class BoardDto {

    private Long id;

    private String writer;

    // 게시물 제목
    private String title;

    // 내용
    private String contents;

    // 최초 작성 시간
    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    private Long userId;

    public Board toEntity(){
        return Board.builder()
                .title(title)
                .contents(contents)
                .createTime(createTime)
                .updateTime(LocalDateTime.now())
                .build();
    }

    public static BoardDto toBoardDto(Board board){
        return new BoardDto(
                board.getId(),
                board.getUser().getName(),
                board.getTitle(),
                board.getContents(),
                board.getCreateTime(),
                board.getUpdateTime(),
                board.getUser().getId());
    }
}
