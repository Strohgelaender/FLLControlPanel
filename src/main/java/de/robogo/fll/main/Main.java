package de.robogo.fll.main;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ComponentScan;

import de.robogo.fll.gui.ControlApplication;

@SpringBootApplication
@ComponentScan("de.robogo.fll")
public class Main {

	public static void main(String[] args) {
		ConfigurableApplicationContext context = SpringApplication.run(Main.class, args);
		ControlApplication.launchApp(ControlApplication.class, context, args);
	}
}
