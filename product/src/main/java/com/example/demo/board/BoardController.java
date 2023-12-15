package com.example.demo.board;

import com.example.demo.core.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

@RequiredArgsConstructor
@Controller
@RequestMapping("/board")
public class BoardController {
    private final BoardService boardService;

    // 게시글 작성 화면으로 이동
    @GetMapping("/create")
    public String create(){
        return "boardCreate";
    }

    // 선택한 게시글로 이동
    @GetMapping("/{id}")
    public String paging(@PathVariable Long id, Model model,
                         @PageableDefault(page = 1) Pageable pageable){
        // 선택한 게시글 가져오기
        BoardDto dto = boardService.findById(id);

        // 게시글 보기용
        model.addAttribute("board", dto);
        // 목록으로 돌아가기용
        model.addAttribute("page", pageable.getPageNumber());
        // 파일 다운로드용
        model.addAttribute("files", boardService.byBoardFiles(id));

        return "BoardDetail";
    }

    // 게시글 저장(단일 / 다중 파일)
    @PostMapping("/save")
    public String save(@ModelAttribute BoardDto boardDto,
                       @RequestParam MultipartFile[] files,
                       @AuthenticationPrincipal CustomUserDetails customUserDetails) throws IOException {
        boardService.save(boardDto, files, customUserDetails.getUser());

        return "redirect:/board/paging";
    }

    @GetMapping("/update/{id}")
    public String updateForm(@PathVariable Long id, Model model){
        BoardDto dto = boardService.findById(id);
        model.addAttribute("board", dto);
        return "boardUpdate";
    }

    @PostMapping("/update")
    public String update(@ModelAttribute BoardDto boardDto,
                         @RequestParam MultipartFile[] files) throws IOException {
        boardService.update(boardDto, files);
        return "redirect:/board/";
    }

    @PostMapping("/delete/{id}")
    public String delete(@PathVariable Long id){
        boardService.delete(id);

        return "redirect:/board/paging";
    }

    @GetMapping(value = {"/paging", "/"})
    public String paging(@PageableDefault(page = 1) Pageable pageable, Model model){
        Page<BoardDto> boards = boardService.paging(pageable);

        int blockLimit = 3;
        int startPage = (int)(Math.ceil((double)pageable.getPageNumber() / blockLimit) - 1) * blockLimit + 1;
        int endPage = Math.min((startPage + blockLimit - 1), boards.getTotalPages());

        model.addAttribute("boardList", boards);
        model.addAttribute("startPage", startPage);
        model.addAttribute("endPage", endPage);

        return "paging";
    }
}
