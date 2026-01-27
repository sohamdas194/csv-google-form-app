package com.flipkart.csv_google_form_app;

import com.flipkart.csv_google_form_app.ui.CsvUploaderUI;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;

import javax.swing.*;

@SpringBootApplication
public class CsvGoogleFormAppApplication {

	public static void main(String[] args) {
		SpringApplication.run(CsvGoogleFormAppApplication.class, args);
	}

	@EventListener(ApplicationReadyEvent.class)
	public void launchUI(ApplicationReadyEvent event) {

		SwingUtilities.invokeLater(() -> {
			CsvUploaderUI ui = event.getApplicationContext().getBean(CsvUploaderUI.class);
			ui.setVisible(true);
		});
	}

}
