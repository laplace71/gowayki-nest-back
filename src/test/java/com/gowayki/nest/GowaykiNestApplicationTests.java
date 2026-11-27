package com.gowayki.nest;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = {"app.jwt.secret=test-secret-para-la-suite-local-32-bytes"})
class GowaykiNestApplicationTests {

	@Test
	void contextLoads() {
	}

}
