package com.example.demo.comment;

import com.example.demo.board.Board;
import com.example.demo.user.User;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
public class Comment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 50)
    private String contents;

    @Column(length = 25, nullable = false)
    private LocalDateTime createTime;

    @Column(length = 25, nullable = false)
    private LocalDateTime updateTime;

    // 1 : 다 연관 관계
    // 지연로딩
    // 연관관계 생성 시 넣어질 이름
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "board_id")
    private Board board;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Builder
    public Comment(Long id, String contents, LocalDateTime createTime, LocalDateTime updateTime, Board board, User user) {
        this.id = id;
        this.contents = contents;
        this.createTime = createTime;
        this.updateTime = updateTime;
        this.board = board;
        this.user = user;
    }

    public void updateFromDto(CommentDto commentDto){
        this.contents = commentDto.getContents();
        this.updateTime = LocalDateTime.now();
    }
}
