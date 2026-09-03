package com.edgareldy.springsecuritytutorial;

import org.springframework.boot.SpringApplication;

public class TestSpringSecurityTutorialApplication {

	public static void main(String[] args) {
		SpringApplication.from(SpringSecurityTutorialApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
