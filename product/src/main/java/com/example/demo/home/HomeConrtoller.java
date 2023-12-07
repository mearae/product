package com.example.demo.home;

import com.example.demo.user.User;
import com.example.demo.user.UserResponse;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

@Controller
public class HomeConrtoller {
    @GetMapping("/")
    public String home(HttpServletRequest req){
        HttpSession session = req.getSession();
        if (session.getAttribute("token") != null)
            return "logined";
        return "index";
    }

    @GetMapping("/join")
    public String joinForm() {
        return "join";
    }
}
