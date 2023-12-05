package com.example.demo.home;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

@Controller
public class HomeConrtoller {
    @GetMapping("/")
    public String home(HttpServletRequest req){
        HttpSession session = req.getSession();
        if (session.getAttribute("access_token") != null)
            return "logined";
        return "index";
    }

    @GetMapping("/join")
    public String joinForm() {
        return "join";
    }
}
