package com.stockleague.backend.openapi.controller;

import com.stockleague.backend.openapi.service.OpenApiService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/openapi")
public class OpenApiController {

    private final OpenApiService openApiService;


}
