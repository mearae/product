package com.example.demo.board;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
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

    @GetMapping("/create")
    public String create(){
        return "create";
    }

    @GetMapping("/{id}")
    public String paging(@PathVariable Long id, Model model,
                         @PageableDefault(page = 1) Pageable pageable){
        BoardDto dto = boardService.findById(id);

        model.addAttribute("board", dto);
        model.addAttribute("page", pageable.getPageNumber());
        model.addAttribute("files", boardService.byBoardFiles(id));

        return "detail";
    }

    @PostMapping("/save")
    public String save(@ModelAttribute BoardDto boardDto,
                       @RequestParam MultipartFile[] files, HttpServletRequest req) throws IOException {
        boardService.save(boardDto, files, req.getSession());

        return "redirect:/board/paging";
    }

    @GetMapping("/update/{id}")
    public String updateForm(@PathVariable Long id, Model model){
        BoardDto dto = boardService.findById(id);
        model.addAttribute("board", dto);
        return "update";
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
        int endPage = ((startPage + blockLimit - 1) < boards.getTotalPages()) ? (startPage + blockLimit - 1): boards.getTotalPages();

        model.addAttribute("boardList", boards);
        model.addAttribute("startPage", startPage);
        model.addAttribute("endPage", endPage);

        return "paging";
    }
}
