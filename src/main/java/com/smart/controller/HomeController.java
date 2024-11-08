package com.smart.controller;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.smart.entities.User;
import com.smart.helper.FileUploadHelper;
import com.smart.services.UserServices;

@Controller
@RequestMapping("/")
public class HomeController {
	
	@Autowired
	private BCryptPasswordEncoder passwordEncoder;
	
	@Autowired
	private UserServices userServices;
	
	@Autowired
	private FileUploadHelper fileUploadHelper;
	
	@RequestMapping("/")
	public String getHome(Model model) {
		
		model.addAttribute("title","Home - Smart Contact Manager");
		return "home";
	}

	@RequestMapping("/home")
	public String home(Model model) {
		
		model.addAttribute("title","Home - Smart Contact Manager");
		return "home";
	}
	
	@RequestMapping("/about")
	public String about(Model model) {
		model.addAttribute("title", "About - Smart Contact Manager");
		return "about";
	}
	
	@RequestMapping("/signin")
	public String login(Model model) {
		model.addAttribute("title", "Log In - Smart Contact Manager");
		return "login";
	}
	
	
	@RequestMapping("/signup")
	public String signup(Model model) {
		model.addAttribute("title", "Sign Up - Smart Contact Manager");
		model.addAttribute("user", new User()); 		
		return "signup";
	}
	
	@PostMapping("/process")
	public String processSignUp(@Valid @ModelAttribute("user") User user, BindingResult result,
			@RequestParam(value = "agreement", defaultValue = "false" ) boolean agreement, 
			@RequestParam("file") MultipartFile file,  
			 Model model) {
		
		try {
			
			String imageFile;
				
			System.out.println("Agreement:"+agreement);
			System.out.println("User:"+user);
			
		user.setPassword(passwordEncoder.encode(user.getPassword()));
			
			if(result.hasErrors()) {
				System.out.println("Errors:"+result.toString());
				model.addAttribute("user", user);
				return "signup";
			}
			
			
			if(!agreement) {
			
				System.out.println("Please click on Terms and Conditions.");
				return "signup";
			
			}  else {
				
				boolean uploadedFile = fileUploadHelper.uploadFile(file);
				
				if (uploadedFile) {
					System.out.println("Image is uploaded successfully.");
					
					imageFile = ServletUriComponentsBuilder.fromCurrentContextPath()
							.path("/img/")
							.path(file.getOriginalFilename())
							.toUriString();
					
					user.setImageUrl(imageFile);
					
					System.out.println(imageFile);				
				    }		
				
				User r = userServices.addUser(user);	
				System.out.println("Saved User:"+ r);
				model.addAttribute("user",r);
				
				return "success";
			}
			
			} catch (Exception e) {
				
				e.printStackTrace();
				return "signup";
			}
		
	}
	
}
