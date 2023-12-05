package com.example.demo.boardFile;

import com.example.demo.board.Board;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
public class BoardFile {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String filePath;

    private String fileName;

    private String uuid;

    private String fileType;

    private Long fileSize;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "board_id")
    private Board board;

    @Builder
    public BoardFile(Long id, String filePath, String fileName, String uuid, String fileType, Long fileSize, Board board) {
        this.id = id;
        this.filePath = filePath;
        this.fileName = fileName;
        this.uuid = uuid;
        this.fileType = fileType;
        this.fileSize = fileSize;
        this.board = board;
    }

    public void updateFromBoard(Board board){
        this.board = board;
    }
}
