package com.example.qlgear.controller;

import com.example.qlgear.entity.User;
import com.example.qlgear.repository.UserRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class LoginController {
    private final UserRepository repo;
    public LoginController(UserRepository repo){
        this.repo=repo;
    }
    //page login
    @GetMapping("/login")
    public String login(){
        return "login";
    }
    //logic login
    @PostMapping("/login")
    public String login(@RequestParam String username,
                        @RequestParam String password,
                        HttpSession session){
        User user=repo.findByUsernameAndPassword(username,password);
        if(user!=null){
            session.setAttribute("user",user);
            session.setAttribute("role",user.getRole());
            if ("ADMIN".equals(user.getRole()) || "STAFF".equals(user.getRole())) {
                return "redirect:/home";
            } else {
                return "redirect:/";
            }
        }
        return "redirect:/login";
    }
    //log out
    @GetMapping("/logout")
    public String logout(HttpSession session){
        session.invalidate();
        return "redirect:/login";
    }
}
