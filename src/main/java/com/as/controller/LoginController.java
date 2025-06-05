package com.as.controller;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class LoginController {
    @GetMapping("/login")
    public String login() {
        return "login";
    }
    
    @GetMapping("/") // ルートURL ("/") に対するGETリクエストを処理します
    public String redirectToIndex() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication(); // 現在のユーザーの認証情報を取得します
        if (authentication != null && authentication.isAuthenticated()) {
            return "redirect:/index";
        }
        return "redirect:/login";
    }
    
    @GetMapping("/index")
    public String index() {
        return "index";
    }
}
