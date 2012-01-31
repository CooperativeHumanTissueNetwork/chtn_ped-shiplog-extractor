package nch.chtn.shiplog.extractor;


import java.util.logging.Level;

import lombok.extern.java.Log;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

@Log
public class CommandLineRunner {
	public static void main(String[] args) {
		log.info("Executing command line interface for shiplog extractor");
		ApplicationContext context = new ClassPathXmlApplicationContext("spring.xml");
		ShiplogExtractor extractor = (ShiplogExtractor) context.getBean("extractor");
		try {
			extractor.Run();
		} catch (Exception e) {
			e.printStackTrace();
			log.log(Level.SEVERE, e.getMessage(), e);
			System.exit(1);
		}
	}
}
