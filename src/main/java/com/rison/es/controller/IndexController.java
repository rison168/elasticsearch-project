package com.rison.es.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class IndexController {
    /**
     *
     * @return
     */
    @GetMapping("/index")
    private String index(){
        return "jd";
    }
}
