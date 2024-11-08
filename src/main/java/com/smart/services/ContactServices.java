package com.smart.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.smart.dao.ContactRepository;
import com.smart.entities.Contact;

@Service
public class ContactServices {
	
	@Autowired
	ContactRepository contactRepository;
	
	public Contact addContact(Contact contact) {
		
		Contact result = this.contactRepository.save(contact);
		
		return result;
	}
	
}
