package com.example.data_enrichment_service;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

@Import(TestcontainersConfiguration.class)
@SpringBootTest(properties = {
		"app.enrichment-api.stub=true",
		"spring.jpa.hibernate.ddl-auto=create-drop"
})
class DataEnrichmentServiceApplicationTests {

	@Test
	void contextLoads() {
	}
}
