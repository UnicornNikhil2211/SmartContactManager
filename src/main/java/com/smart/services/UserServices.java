package com.smart.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.smart.dao.UserRepository;
import com.smart.entities.User;

@Service
public class UserServices {

		@Autowired
		UserRepository userRepository;
		
		public User addUser(User user) {
			
			User result = this.userRepository.save(user);
			
			return result;
		}
	
}
