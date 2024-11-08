package com.smart.controller;


import java.awt.Image;
import java.security.Principal;
import java.util.Optional;

import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import javax.xml.stream.events.EndDocument;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.smart.dao.ContactRepository;
import com.smart.dao.UserRepository;
import com.smart.entities.Contact;
import com.smart.entities.User;
import com.smart.helper.FileDeleteHelper;
import com.smart.helper.FileUploadHelper;
import com.smart.helper.SmartMessage;

@Controller
@RequestMapping("/user")
public class UserController {

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private FileUploadHelper fileUploadHelper;
	
	@Autowired
	private FileDeleteHelper fileDeleteHelper;
	
	/*
	 * @Autowired private ContactServices contactServices;
	 */
	
	@Autowired
	private ContactRepository contactRepository;
	 

	@ModelAttribute
	public void commonCode(Model model, Principal principal) {

		String username = principal.getName();

		User user = userRepository.getUserByUserName(username);

		model.addAttribute("user", user);
	}

	@RequestMapping("/index")
	public String dashboard(Model model, Principal principal) {

		model.addAttribute("title", "User Dashboard - Smart Contact Manager");

		return "normal/user_dashboard";
	}

	@RequestMapping("/addcontact")
	public String addContact(Model model) {

		model.addAttribute("title", "Add Contact - Smart Contact Manager");

		model.addAttribute("contact", new Contact());

		return "normal/AddContact";
	}

	@PostMapping("/process-contact")
	public String processContact(@Valid @ModelAttribute("contact") Contact contact, BindingResult result,
			@RequestParam("profileImage") MultipartFile file, Principal principal, HttpSession session, Model model) {

		try {
			
			String profileImage;

			/*
			 * if(result.hasErrors()) { model.addAttribute("contact", contact); return
			 * "normal/AddContact"; }
			 * 
			 * 
			 * Contact c = contactServices.addContact(contact);
			 * System.out.println("Contact:"+ contact); model.addAttribute("contact", c);
			 */
			
			if(file.isEmpty()) {
				//if file is empty then try error message
				System.out.println("Profile image is empty.");
				contact.setImage("default-profile-picture.png");
				
			} else {
				
				boolean uploadedFile = fileUploadHelper.uploadFile(file);
				
				if (uploadedFile) {
					System.out.println("Image is uploaded successfully.");
					
					profileImage = ServletUriComponentsBuilder.fromCurrentContextPath()
							.path("/img/")
							.path(file.getOriginalFilename())
							.toUriString();
					
					contact.setImage(file.getOriginalFilename()  );
					
					System.out.println(profileImage);				
				    }
				
			}
			
			String username = principal.getName();
			
			User user = userRepository.getUserByUserName(username);
			
			contact.setUser(user);
			
			user.getContacts().add(contact);
			
			this.userRepository.save(user);
			
			System.out.println("Contact added to database.");

			//success message
			session.setAttribute("message", new SmartMessage("Your contact is added.", "success"));
			
			return "normal/success";

		} catch (Exception e) {
			System.out.println(e);
			
			//error message
			session.setAttribute("message", new SmartMessage("Something went wrong.", "danger"));
			
			
			return "normal/AddContact";
		}

	}
	
	@GetMapping("/show-contacts/{page}")
	public String showContacts(@PathVariable("page")Integer page, Model model, Principal principal) {
		
		model.addAttribute("title", "View Contacts - Smart Contact Manager");
			
		String username = principal.getName();
		User user = userRepository.getUserByUserName(username);
		
		//current page
		//contact per page = 5
		Pageable pageable = PageRequest.of(page, 5);
		
		//List<Contact> contacts =  this.contactRepository.findContactsByUser(user.getId());
		
		Page<Contact> contacts =  this.contactRepository.findContactsByUser(user.getId(), pageable);
		
		model.addAttribute("contacts", contacts);
		
		model.addAttribute("currentPage", page);
		System.out.println("currentPage:"+ page);
		
		model.addAttribute("totalPages", contacts.getTotalPages());
		System.out.println("totalPages:"+ contacts.getTotalPages());
		
		return "normal/show-contacts";
	}
	
	@GetMapping("/contact/{id}")
	public String contactDetails(@PathVariable("id")Integer id, Principal principal, Model model) {
		try {
		model.addAttribute("title", "Contact Details - Smart Contact Manager");
		
		String username = principal.getName();
		User user = userRepository.getUserByUserName(username);
		
		Optional<Contact> contactOptional = contactRepository.findById(id);
		Contact contact = contactOptional.get();	
		
		System.out.println("Login user_id: "+user.getId());
		System.out.println("Contact View: "+contact.getUser().getId());
		
		
		if(user.getId() == contact.getUser().getId()) {		
			model.addAttribute("contact", contact);	
		}
					
		return "normal/contact-details";
		
		} catch (Exception e) {
			model.addAttribute("contact", null);	
			
			System.err.println(e);
			
			return "normal/contact-details";
		}
	}
	
	@GetMapping("/delete-contact/{id}")
	public String deleteContact(@PathVariable("id") Integer id, Principal principal, Model model, HttpSession httpSession) {
		
		model.addAttribute("title", "Delete Contact - Smart Contact Manager");
		
		String username = principal.getName();
		User user = userRepository.getUserByUserName(username);
		
		Optional<Contact> contactOptional = contactRepository.findById(id);
		Contact contact = contactOptional.get();
		
		System.out.println("Login user_id: "+user.getId());
		System.out.println("Contact View: "+contact.getUser().getId());
		
		
		if(user.getId() == contact.getUser().getId()) {	
			
			//contact.setUser(null);
			
			boolean deleteFile = fileDeleteHelper.deleteFile(contact.getImage());
			System.out.println("File Deleted status:" + deleteFile);
			
			
			contactRepository.deleteById(id);
			
			httpSession.setAttribute("message", new SmartMessage("Contact deleted successfully.", "success"));
			
			System.out.println("Contact is deleted of Contact Id:"+id);
					
		}
		
		return "redirect:/user/show-contacts/0";
	}
	
	@RequestMapping("/update-contact/{id}")
	public String updateContact(@PathVariable("id") Integer id,  Model model) {
		try {
		model.addAttribute("title", "Update Contact - Smart Contact Manager");
		
		Optional<Contact> contactOptional = contactRepository.findById(id);
		Contact contact = contactOptional.get();
		
		
		model.addAttribute("contact", contact);
		
		//System.out.println(contact);

		return "normal/UpdateContact";
		} catch (Exception e) {
			model.addAttribute("contact", null);	
			
			System.err.println(e);
			return "normal/UpdateContact";
		}
	}
	
	// update-contact by id
	@PostMapping("/update-process/{id}")
	public String updateContact(@Valid @ModelAttribute("contact") Contact contact, BindingResult result, @PathVariable("id") Integer id,
			@RequestParam("profileImage") MultipartFile file, Principal principal, HttpSession httpSession, Model model) {

		try {
			
			String username = principal.getName();
			User user = userRepository.getUserByUserName(username);
			
			System.out.println("URL ID:"+ id);
			System.out.println(contact);
			
			Optional<Contact> contactOptional = contactRepository.findById(id);
			Contact old_contact = contactOptional.get();
			
		 if(old_contact.getCid()==id)	{		
			 
			    boolean deleteFile = fileDeleteHelper.deleteFile(contact.getImage());
				System.out.println("File Deleted status:" + deleteFile);
				
				/* profile Image uploaded */ 
				if(file.isEmpty()) {
					//if file is empty then try error message
					System.out.println("Profile image is empty.");
					contact.setImage("default-profile-picture.png");		
				} else {
					
					boolean uploadedFile = fileUploadHelper.uploadFile(file);
					
					if (uploadedFile) {
						System.out.println("Image is uploaded successfully." + file.getOriginalFilename());
										
						contact.setImage(file.getOriginalFilename()  );
							
					    }			
				}
	
				/* End profile Image uploading */
			 
			 
				contact.setCid(id);
				contact.setUser(user);
				
				contactRepository.save(contact);
				System.out.println("Contact updated in database.");
		 }
							
		return "normal/success";
		
		} catch (Exception e) {
			System.out.println(e);
			
			//error message
			httpSession.setAttribute("message", new SmartMessage("Something went wrong.", "danger"));
			
			return "normal/UpdateContact";
		}

	}
	
}
