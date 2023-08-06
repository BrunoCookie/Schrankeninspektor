package com.BrunoCookie.Schrankeninspektor;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
@RestController
public class SchrankeninspektorApplication {

	public static void main(String[] args) {
		SpringApplication.run(SchrankeninspektorApplication.class, args);
	}

	@GetMapping("/ping")
	public String ping(){
		return "Pong!";
	}
}
