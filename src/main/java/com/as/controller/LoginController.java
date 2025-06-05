package com.as.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.as.entity.User;
import com.as.service.UserPrincipal;
import com.as.service.UserService;

@Controller
public class LoginController {
 
    @Autowired
    private UserService userService;
    
    @GetMapping("/login")
    public String login() {
        return "login";
    }
    
    @GetMapping("/") // ルートURL ("/") に対するGETリクエストを処理します
    public String redirectToIndex(Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication(); // 現在のユーザーの認証情報を取得します
        if (authentication != null && authentication.isAuthenticated()) {
            return "redirect:/index";
        }
        return "redirect:/login";
    }
    
    @GetMapping("/index")
    public String index(Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            Object principal = authentication.getPrincipal();
            if (principal instanceof UserPrincipal userPrincipal) {
                model.addAttribute("userName", userPrincipal.getUsername());
                // model.addAttribute("bestRecord", userPrincipal.getUser().getBestRecord()); getUserを作る方法もあったが、今回は下のようにgetBestRecordをUserPrincipalに実装した
                model.addAttribute("bestRecord", userPrincipal.getBestRecord());

                List<User> users = userService.findAllByOrderByBestRecordDesc();
                model.addAttribute("allUsers", users);
            }
        }
        return "index";
    }
}
