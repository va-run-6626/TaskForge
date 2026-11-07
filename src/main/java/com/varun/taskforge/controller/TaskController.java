package com.varun.taskforge.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.varun.taskforge.service.ITaskService;

@RestController
@RequestMapping("/tasks")
public class TaskController {
    private final ITaskService taskService;
}
