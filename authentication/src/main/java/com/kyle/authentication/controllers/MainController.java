package com.kyle.authentication.controllers;

import javax.servlet.http.HttpSession;
import javax.validation.Valid;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.kyle.authentication.models.User;
import com.kyle.authentication.services.UserService;
import com.kyle.authentication.validators.UserValidator;

@Controller
public class MainController {

	@Autowired
	private UserValidator validator;

	private final UserService userService;

	public MainController(UserService userService) {
		this.userService = userService;
	}

	@RequestMapping("/registration")
	public String registerForm(@ModelAttribute("user") User user) {
		return "registrationPage.jsp";
	}

	@RequestMapping("/login")
	public String login() {
		return "loginPage.jsp";
	}

	@RequestMapping(value = "/registration", method = RequestMethod.POST)
	public String registerUser(@Valid @ModelAttribute("user") User user, BindingResult result, HttpSession session) {
		validator.validate(user, result);
		if (result.hasErrors()) {
			return "registrationPage.jsp";
		}
		User newUser = userService.registerUser(user);
		// only put id in session
		session.setAttribute("user_id", newUser.getId());
		return "redirect:/home";
	}

	@RequestMapping(value = "/login", method = RequestMethod.POST)
	public String loginUser(@RequestParam("email") String email, @RequestParam("password") String password, Model model,
			HttpSession session, RedirectAttributes flash) {
		//boolean isAuthenticated = userService.authenticateUser(email, password);
		if (userService.authenticateUser(email, password)) {
			flash.addFlashAttribute("success", "cool");
			User newUser = userService.findByEmail(email);
			session.setAttribute("user_id", newUser.getId());
			return "redirect:/home";
		}
		flash.addFlashAttribute("error", "Invalid Credentials");
		return "redirect:/login";
	}

	@RequestMapping("/home")
	public String home(HttpSession session, Model model) {
		// get user from session, save them in the model and return the home page

		Long id = (Long) session.getAttribute("user_id");
		if (id != null) {
			User thisUser = userService.findUserById(id);
			model.addAttribute("user", thisUser);
			return "homePage.jsp";
		}
		return "redirect:/login";

	}

	@RequestMapping("/logout")
	public String logout(HttpSession session) {
		session.invalidate();
		return "redirect:/login";
	}

}