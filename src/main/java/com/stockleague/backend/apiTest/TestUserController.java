package com.stockleague.backend.apiTest;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/test")
public class TestUserController {

    private final TestUserService testUserService;

    @PostMapping("/user")
    public List<TestUserService.TokenItem> issueToken(){
        return testUserService.testUserSignUp();
    }
}
