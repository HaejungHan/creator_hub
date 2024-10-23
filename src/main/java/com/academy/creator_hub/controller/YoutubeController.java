package com.academy.creator_hub.controller;


import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
public class YoutubeController {
    @GetMapping("/home")
    public String home(Model model) {
        model.addAttribute("name", "Your Name");
        model.addAttribute("list", List.of("Item 1", "Item 2", "Item 3"));
        return "index"; // index.jsp를 반환
    }
}

