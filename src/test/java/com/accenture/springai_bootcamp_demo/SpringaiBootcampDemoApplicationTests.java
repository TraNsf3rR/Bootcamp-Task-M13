package com.accenture.springai_bootcamp_demo;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = {
        "spring.ai.ollama.base-url=http://127.0.0.1:1",
        "spring.datasource.url=jdbc:sqlite:target/test-chatbot.db"
})
class SpringaiBootcampDemoApplicationTests {

	@Test
	void contextLoads() {
	}

}
