package com.example.demo.boardFile;

import lombok.*;

@Setter
@Getter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class FileDto {

    private String filePath;

    private String fileName;

    private String fileType;

    // 랜덤 키
    private String uuid;

    private Long fileSize;

    private Long boardId;

    public BoardFile toEntity(){
        return BoardFile.builder()
                .filePath(filePath)
                .fileName(fileName)
                .uuid(uuid)
                .fileType(fileType)
                .fileSize(fileSize)
                .build();
    }

    public static FileDto toFileDto(BoardFile boardFile){
        return new FileDto(
                boardFile.getFilePath(),
                boardFile.getFileName(),
                boardFile.getUuid(),
                boardFile.getFileType(),
                boardFile.getFileSize(),
                boardFile.getBoard().getId());
    }
}
