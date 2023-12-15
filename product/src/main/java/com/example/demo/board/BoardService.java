package com.example.demo.board;

import com.example.demo.boardFile.BoardFile;
import com.example.demo.boardFile.FileRepository;
import com.example.demo.user.User;
import com.example.demo.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Transactional(readOnly = true)
@RequiredArgsConstructor
@Service
public class BoardService {
    private final UserService userService;

    private final BoardRepository boardRepository;

    private final FileRepository fileRepository;

    // 집에서 본인 PC 이름 및 경로로 설정
    private String filePath = "C:/Users/G/Desktop/GitHub/shopBoard/board/Board Files/";

    public BoardDto findById(Long id){
        Board board = boardRepository.findById(id).get();

        return BoardDto.toBoardDto(board);
    }

    @Transactional
    public void save(BoardDto boardDto, MultipartFile[] files, User user) throws IOException{

        boardDto.setCreateTime(LocalDateTime.now());
        // 게시글 DB에 저장 후 PK 받아옴
        Long id = boardRepository.save(boardDto.toEntity()).getId();
        Board board = boardRepository.findById(id).orElseThrow();
        board.updateFromUser(user);

        // 추가
        if (!files[0].isEmpty()) {
            Path uploadPath = Paths.get(filePath);

            // 만약 경로가 없다면... 경로 생성
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }
            for (MultipartFile file : files) {
                // 파일명 추출
                String originalFilename = file.getOriginalFilename();

                // 확장자 추출
                String formatType = originalFilename.substring(
                        originalFilename.lastIndexOf("."));

                // UUID 생성
                String uuid = UUID.randomUUID().toString();

                // 경로 지정
                String path = filePath + uuid + originalFilename;

                // 파일을 물리적으로 저장 (DB에 저장 X)
                file.transferTo( new File(path) );

                BoardFile boardFile = BoardFile.builder()
                        .filePath(filePath)
                        .fileName(originalFilename)
                        .uuid(uuid)
                        .fileType(formatType)
                        .fileSize(file.getSize())
                        .board(board)
                        .build();

                fileRepository.save(boardFile);
            }
        }
    }

    @Transactional
    public void update(BoardDto boardDto, MultipartFile[] files) throws IOException {
        Optional<Board> boardOptional = boardRepository.findById(boardDto.getId());
        Board board = boardOptional.get();

        board.updateFromDto(boardDto);
        board.clearFile();
        boardRepository.save(board);

        fileRepository.deleteByBoard_id(board.getId());
        if (!files[0].isEmpty()) {
            Path uploadPath = Paths.get(filePath);

            // 만약 경로가 없다면... 경로 생성
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            for (MultipartFile file : files) {
                // 파일명 추출
                String originalFilename = file.getOriginalFilename();

                // 확장자 추출
                String formatType = originalFilename.substring(
                        originalFilename.lastIndexOf("."));

                // UUID 생성
                String uuid = UUID.randomUUID().toString();

                // 경로 지정
                String path = filePath + uuid + originalFilename;

                // 파일을 물리적으로 저장 (DB에 저장 X)
                file.transferTo( new File(path) );

                BoardFile boardFile = BoardFile.builder()
                        .filePath(filePath)
                        .fileName(originalFilename)
                        .uuid(uuid)
                        .fileType(formatType)
                        .fileSize(file.getSize())
                        .board(board)
                        .build();

                fileRepository.save(boardFile);
            }
        }
    }

    @Transactional
    public void delete(Long id) {
        boardRepository.deleteById(id);
    }

    // paging용 함수
    public Page<BoardDto> paging(Pageable pageable){

        // 페이지 시작 번호 설정
        int page = pageable.getPageNumber() - 1;
        // 페이지에 호환될 게시물 개수
        int size = 5;

        // 전체 게시물 불러오기(수량별로 정렬)
        Page<Board> boards = boardRepository.findAll(PageRequest.of(page, size));

        return boards.map(board -> new BoardDto(
                board.getId(),
                board.getUser().getName(),
                board.getTitle(),
                board.getContents(),
                board.getCreateTime(),
                board.getUpdateTime(),
                board.getUser().getId()));
    }

    public List<BoardFile> byBoardFiles(Long id){
        return fileRepository.findByBoard_id(id);
    }
}
