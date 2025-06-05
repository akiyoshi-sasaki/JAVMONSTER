package com.as.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.ModelAndView;

import com.as.entity.User;
import com.as.entity.UserDto;
import com.as.service.UserService;

@Controller
public class RegisterController {

    @Autowired
    private UserService userService;

    @GetMapping("/register")
    public ModelAndView registerForm() {
        ModelAndView mav = new ModelAndView();
        mav.addObject("user", new UserDto());
        mav.setViewName("register");
        return mav; // ModelAndViewオブジェクトを返します
    }

    @PostMapping("/register")
    public String register(@ModelAttribute UserDto userDto) {
        System.out.println(userDto);
        User existing = userService.findByUsername(userDto.getUserName());
        if(existing != null){
            return "register";
        }
        userService.save(userDto);
        return "login";
    }
}
