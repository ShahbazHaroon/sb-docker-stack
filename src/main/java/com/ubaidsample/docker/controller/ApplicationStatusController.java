/*
 * @author Muhammad Ubaid Ur Raheem Ahmad AKA Shahbaz Haroon
 * Email: shahbazhrn@gmail.com
 * Cell: +923002585925
 * GitHub: https://github.com/ShahbazHaroon
 */

package com.ubaidsample.docker.controller;

import com.ubaidsample.docker.dto.response.ApplicationStatusResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/v1/status")
public class ApplicationStatusController {

    @Value("${spring.application.name}")
    private String applicationName;

    @GetMapping({"", "/"})
    public ApplicationStatusResponse getApplicationStatus() {
        log.info("ApplicationStatusController -> getApplicationStatus() called");
        return new ApplicationStatusResponse(applicationName, "up and running");
    }
}