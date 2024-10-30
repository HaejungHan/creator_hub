package com.academy.creator_hub.view;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ViewController {
    @GetMapping("/main")
    public String mainPage() {
        return "index";
    }
}
