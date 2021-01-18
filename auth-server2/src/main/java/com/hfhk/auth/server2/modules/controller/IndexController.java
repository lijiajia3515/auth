package com.hfhk.auth.server2.modules.controller;

import com.hfhk.auth.server2.domain.AuthUser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Slf4j
@Controller
@RequestMapping
public class IndexController {
	@GetMapping
	public String index(@AuthenticationPrincipal AuthUser user, Model model) {
		model.addAttribute("user", user);
		return "index";
	}

	@GetMapping("/login")
	public String login() {
		return "login";
	}


}