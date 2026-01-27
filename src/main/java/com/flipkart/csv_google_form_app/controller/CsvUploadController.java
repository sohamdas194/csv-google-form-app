package com.flipkart.csv_google_form_app.controller;

import com.flipkart.csv_google_form_app.service.GoogleFormService;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/upload")
public class CsvUploadController {

    private static final org.slf4j.Logger log =
            org.slf4j.LoggerFactory.getLogger(CsvUploadController.class);

    private final GoogleFormService formService;

    public CsvUploadController(GoogleFormService formService) {
        this.formService = formService;
    }

    @PostMapping
    public String upload(@RequestParam("file") MultipartFile file) throws Exception {

        log.info("UPLOAD STARTED: {}", file.getOriginalFilename());

        Reader reader = new InputStreamReader(file.getInputStream());
        CSVParser parser = CSVFormat.DEFAULT
                .withFirstRecordAsHeader()
                .parse(reader);

        String timestamp = java.time.LocalDateTime.now()
                .format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));

        // Output files
        Path passFile = Paths.get("pass_rows_" + timestamp + ".csv");
        Path failFile = Paths.get("fail_rows_" + timestamp + ".csv");

        BufferedWriter passWriter = Files.newBufferedWriter(
                passFile, StandardOpenOption.CREATE, StandardOpenOption.APPEND);

        BufferedWriter failWriter = Files.newBufferedWriter(
                failFile, StandardOpenOption.CREATE, StandardOpenOption.APPEND);

        // CSV headers
        List<String> headers = new ArrayList<>(parser.getHeaderMap().keySet());

        // Write headers once
        passWriter.write(String.join(",", headers));
        passWriter.newLine();

        failWriter.write(String.join(",", headers) + ",ErrorReason");
        failWriter.newLine();

        int total = 0;
        int success = 0;
        int failed = 0;

        for (CSVRecord record : parser) {
            total++;

            try {
                // Submit to Google Form
                formService.submit(record);
                success++;

                log.info("PASS | Row {} | Data: {}", total, record.toMap());

                // Write success row
                List<String> row = new ArrayList<>();
                for (String h : headers) row.add(record.get(h));

                passWriter.write(String.join(",", row));
                passWriter.newLine();

            } catch (Exception ex) {
                failed++;

                log.error("FAIL | Row {} | Reason: {} | Data: {}",
                        total, ex.getMessage(), record.toMap());

                // Write failed row + error reason
                List<String> row = new ArrayList<>();
                for (String h : headers) row.add(record.get(h));

                failWriter.write(String.join(",", row) + ",\"" + ex.getMessage() + "\"");
                failWriter.newLine();
            }
        }

        passWriter.close();
        failWriter.close();

        log.info("UPLOAD COMPLETE | Total={} | Success={} | Failed={}",
                total, success, failed);

        return "Uploaded rows: " + total +
                " | Success=" + success +
                " | Failed=" + failed;
    }
}
