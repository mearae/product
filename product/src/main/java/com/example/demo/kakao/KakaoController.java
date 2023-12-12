package com.example.demo.kakao;

import com.example.demo.core.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletResponse;

@RequiredArgsConstructor
@Controller
@RequestMapping("/kakao")
public class KakaoController {

    private final KakaoService kakaoService;

    // 인가코드 받기 : https://kauth.kakao.com/oauth/authorize 는
    // kakao Developer에 보면 Get으로 연결하라고 함
    @GetMapping("/oauth")
    public String kakaoConnect(Error error, HttpStatus status) {
        // "redirect:" -> 뒤에 오는 http 링크(String)로 이동
        // @RestController 일 경우에는 링크 이동이 불가(객체만 return 가능함)
        //              -> 화면에 문자열이 뜸
        // @Controller 일 경우 링크 이동과 객체 return 모두 가능
        String link = kakaoService.kakaoConnect();

        return "redirect:" + link;
    }

    @GetMapping("/relogin")
    public String kakaoAutoConnect(Error error){
        String link = kakaoService.kakaoAutoConnect();

        return "redirect:" + link;
    }

    @GetMapping(value = "/callback", produces = "application/json")
    public String kakaoLogin(@RequestParam("code") String code, Error error,
                             HttpServletResponse res) {
        // 로그인은 크롬 화면에서 하고 여기서 실제로는 토큰, 사용자 정보 얻기를 함
        String link = kakaoService.kakaoLogin(code, res);

        // 다시 로그인 화면으로 돌아옴
        return "redirect:" + link;
    }

    @PostMapping("/logout")
    public String kakaoLogout(@AuthenticationPrincipal CustomUserDetails customUserDetails,
                              HttpServletResponse res) {
        kakaoService.kakaoLogout(customUserDetails.getUser().getId(), res);

        return "index";
    }

    @GetMapping("/fulllogout")
    public String kakaoFullLogout(@AuthenticationPrincipal CustomUserDetails customUserDetails,
                                  HttpServletResponse res){
        String link = kakaoService.kakaoFullLogout(customUserDetails.getUser().getId(), res);

        return "redirect:" + link;
    }

    @PostMapping("/disconnect")
    public String kakaoDisconnect(@AuthenticationPrincipal CustomUserDetails customUserDetails){
        kakaoService.kakaoDisconnect(customUserDetails.getUser().getId());

        return "index";
    }

    @GetMapping("/userlist")
    public String kakaoDisconnect(){
        kakaoService.kakaoUserList();

        return "index";
    }

    @GetMapping("/end")
    public void endServer(){
        kakaoService.endServer();
    }
}