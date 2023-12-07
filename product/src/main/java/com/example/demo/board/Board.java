package com.example.demo.board;

import com.example.demo.boardFile.BoardFile;
import com.example.demo.comment.Comment;
import com.example.demo.user.User;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.LinkedList;
import java.util.List;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
public class Board {

    // PK
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 게시물 제목
    @Column(length = 50, nullable = false)
    private String title;

    // 내용
    @Column
    private String contents;

    // 최초 작성 시간
    @Column(length = 25, nullable = false)
    private LocalDateTime createTime;

    // 최근 수정 시간
    @Column(length = 25, nullable = false)
    private LocalDateTime updateTime;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    // 1 : 다 연관 관계
    // 소유(1)와 비소유(다)(여기에서 확인)
    // cascade = CascadeType.REMOVE : 소유자(게시물)이 삭제될 경우 그 소유물(댓글)이 자동 삭제
    // orphanRemoval = true : 만약 연결 관계가 끊어지면 삭제
    // fetch = FetchType.LAZY : 지연로딩 (성능 최적화)
    @OneToMany(mappedBy = "board", cascade = CascadeType.REMOVE, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Comment> comments = new LinkedList<>();

    @OneToMany(mappedBy = "board", cascade = CascadeType.REMOVE, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<BoardFile> boardFiles = new LinkedList<>();

    @Builder
    public Board(Long id, String title, String contents, LocalDateTime createTime, LocalDateTime updateTime, User user, List<Comment> comments, List<BoardFile> boardFiles) {
        this.id = id;
        this.title = title;
        this.contents = contents;
        this.createTime = createTime;
        this.updateTime = updateTime;
        this.user = user;
        this.comments = comments;
        this.boardFiles = boardFiles;
    }

    public void updateFromDto(BoardDto boardDto) {
        // 모든 변경 사항을 셋팅
        this.title = boardDto.getTitle();
        this.contents = boardDto.getContents();
        this.updateTime = LocalDateTime.now();
    }

    // 연관관계 설정
    public void updateFromUser(User user){
        this.user = user;
    }

    // 게시글 수정 시 이전의 파일들 전부 삭제
    public void clearFile(){this.boardFiles.clear();}
}
