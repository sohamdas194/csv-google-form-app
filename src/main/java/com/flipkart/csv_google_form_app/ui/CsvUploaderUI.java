package com.flipkart.csv_google_form_app.ui;

import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import javax.swing.*;
import java.awt.*;
import java.io.*;

@Component
public class CsvUploaderUI extends JFrame {

    private static final String PORT = "8030";
    private static final String API_UPLOAD = "http://localhost:" + PORT + "/api/upload";

    private JTextArea logArea = new JTextArea();
    private JLabel statusLabel = new JLabel("Ready");
    private JButton uploadBtn = new JButton("Upload CSV");

    public CsvUploaderUI() {
        setTitle("CSV → Google Form (Spring Boot Unified)");
        setSize(520, 380);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        uploadBtn.addActionListener(e -> chooseFile());

        logArea.setEditable(false);

        add(uploadBtn, BorderLayout.NORTH);
        add(new JScrollPane(logArea), BorderLayout.CENTER);
        add(statusLabel, BorderLayout.SOUTH);
    }

    private void chooseFile() {
        JFileChooser chooser = new JFileChooser();

        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            uploadFile(chooser.getSelectedFile());
        }
    }

    private void uploadFile(File file) {

        new Thread(() -> {
            try {
                SwingUtilities.invokeLater(() -> {
                    statusLabel.setText("Uploading...");
                    uploadBtn.setEnabled(false);
                    logArea.append("Uploading: " + file.getName() + "\n");
                });

                RestTemplate restTemplate = new RestTemplate();

                MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
                body.add("file", new FileSystemResource(file));

                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.MULTIPART_FORM_DATA);

                HttpEntity<MultiValueMap<String, Object>> requestEntity =
                        new HttpEntity<>(body, headers);

                ResponseEntity<String> response = restTemplate.postForEntity(
                        API_UPLOAD,
                        requestEntity,
                        String.class
                );

                SwingUtilities.invokeLater(() -> {
                    statusLabel.setText("Upload complete");
                    uploadBtn.setEnabled(true);

                    logArea.append("HTTP " + response.getStatusCode() + "\n");
                    logArea.append("Response:\n" + response.getBody() + "\n");
                });

            } catch (Exception e) {
                SwingUtilities.invokeLater(() -> {
                    statusLabel.setText("Upload failed");
                    uploadBtn.setEnabled(true);
                    logArea.append("Error: " + e.getMessage() + "\n");
                });
            }
        }).start();
    }
}
