package com.eduardobernal.sneakstock;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

@SpringBootTest
@Import(TestcontainersConfiguration.class)
class SneakstockApplicationTests {

	@Test
	void contextLoads() {
	}

}
