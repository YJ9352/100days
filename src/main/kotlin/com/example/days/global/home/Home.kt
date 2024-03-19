package com.example.days.global.home

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class Home {
    @GetMapping("/")
    fun home() = "재밌는 우리집!!"
}