package com.example.demo.comment;

import com.example.demo.board.BoardDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/comment")
public class CommentController {

    private final CommentService commentService;

    @PostMapping("/save")
    public ResponseEntity<CommentDto> save(@ModelAttribute CommentDto commentDto){
        Comment comment = commentService.save(commentDto);

        if (comment != null){
            return ResponseEntity.ok().body(commentDto);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/delete/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id){
        commentService.delete(id);

        return ResponseEntity.ok().build();
    }

    @PostMapping("/update")
    public ResponseEntity<?> update(@ModelAttribute CommentDto commentDto){
        Comment comment = commentService.update(commentDto);
        if (comment != null) {
            return ResponseEntity.ok().body(commentDto);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/comments/{id}")
    public ResponseEntity<List<CommentDto>> commentList(@PathVariable Long id) {
        List<CommentDto> comments = commentService.commentList(id);

        return ResponseEntity.ok().body(comments);
    }
}
