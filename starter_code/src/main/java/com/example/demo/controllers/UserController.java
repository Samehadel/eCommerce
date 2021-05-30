package com.example.demo.controllers;

import com.example.demo.model.persistence.Cart;
import com.example.demo.model.persistence.User;
import com.example.demo.model.persistence.repositories.CartRepository;
import com.example.demo.model.persistence.repositories.UserRepository;
import com.example.demo.model.requests.CreateUserRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/user")
public class UserController {

	private static final Logger logger = LoggerFactory.getLogger(UserController.class);

	@Autowired
	private UserRepository userRepository;
	
	@Autowired
	private CartRepository cartRepository;

	@Autowired
	private BCryptPasswordEncoder encoder;

	@GetMapping("/id/{id}")
	public ResponseEntity<User> findById(@PathVariable Long id) {
		logger.info("Find User By ID Request");

		Optional<User> optionalUser = userRepository.findById(id);

		if (!optionalUser.isPresent())
			return ResponseEntity.badRequest().header("Error", "User ID Not Exist").build();

		return ResponseEntity.of(userRepository.findById(id));
	}
	
	@GetMapping("/{username}")
	public ResponseEntity<User> findByUserName(@PathVariable String username) {
		logger.info("Find User By Username Request");
		User user = userRepository.findByUsername(username);

		return user == null ? ResponseEntity.notFound().build() : ResponseEntity.ok(user);
	}
	
	@PostMapping("/create")
	public ResponseEntity<User> createUser(@RequestBody CreateUserRequest createUserRequest) {
		logger.info("Create User Request");

		User user = new User();
		Cart cart = new Cart();

		cartRepository.save(cart);

		// Check if username already exists
		if(userRepository.findByUsername(createUserRequest.getUsername()) != null)
			return ResponseEntity.badRequest().header("Error", "Username Already Exist").build();


		// Checks the password length and matches with confirmed password
		if( createUserRequest.getPassword().length() < 8 ||
		    !createUserRequest.getPassword().equals(createUserRequest.getConfirmPassword()) ){
			return ResponseEntity.badRequest().build();
		}

		user.setUsername(createUserRequest.getUsername());
		user.setPassword(encoder.encode(createUserRequest.getPassword()));
		user.setCart(cart);

		userRepository.save(user);

		return ResponseEntity.ok(user);
	}
	
}