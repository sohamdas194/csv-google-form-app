package com.flipkart.csv_google_form_app.service;

import org.apache.commons.csv.CSVRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.Optional;

import static com.flipkart.csv_google_form_app.constants.FormInputIDs.*;

@Service
public class GoogleFormService {
//    clone form id
//    private static final String FORM_ID = "1FAIpQLScBg5Z7vndz_8-z4zTZvgbYfKyVNkqgWinXcpslljMKTADe8w";

//    central form id
    private static final String FORM_ID = "1FAIpQLSeEe6x93dTLHLjJmaauMm0CClxMiZlWCc4t2VijB9xN5PuK9Q";

    private static final String FORM_URL =
            "https://docs.google.com/forms/d/e/" + FORM_ID + "/formResponse";

    public static final String[] VERTICALS = {
            "Apparel (All Clothing)",
            "Footwear/Sandal",
            "Helmet",
            "Mosquito Net",
            "Racquet (Badminton)"
    };

    public static final String[] PV_REMARKS_AFTER_REFINISHING = {
            "No issues (PASS)",
            "MBT PASS",
            "Major Stains",
            "Tag issue",
            "Damaged product",
            "Abused product (Non-Refinishable)",
            "Catalog issue",
            "Fake/Wrong Product"
    };
    public static final String OTHER_OPTION = "__other_option__";
    public static final String OTHER_OPTION_RESPONSE = ".other_option_response";

    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${google.form.mock.enabled:false}")
    private boolean mockMode;

    @Value("${google.form.rate.limit.per.second:3}")
    private int rateLimitPerSecond;

    private long lastSubmitTime = 0;

    private static final Logger log = LoggerFactory.getLogger(GoogleFormService.class);

    public synchronized void submit(CSVRecord record) {

        applyRateLimit();

        MultiValueMap<String, String> formData = buildFormData(record);

        if (mockMode) {
            mockSubmit(formData);
        } else {
            realSubmit(formData);
        }
    }

    // ================= RATE LIMIT =================
    private void applyRateLimit() {
        long intervalMs = 1000L / rateLimitPerSecond;
        long now = System.currentTimeMillis();
        long waitTime = intervalMs - (now - lastSubmitTime);

        if (waitTime > 0) {
            try {
                Thread.sleep(waitTime);
            } catch (InterruptedException ignored) {}
        }

        lastSubmitTime = System.currentTimeMillis();
    }

    // ================= FORM DATA =================
    private MultiValueMap<String, String> buildFormData(CSVRecord record) {

        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add(WSN.getInputID(), safe(record, "WSN"));
        formData.add(BRAND_NAME.getInputID(), safe(record, "Brand Name"));
        formData.add(SELLER_ID.getInputID(), safe(record, "Seller ID"));

        Optional<String> vertical = Arrays.stream(VERTICALS).filter(vertcl -> vertcl.equals(safe(record, "Vertical"))).findFirst();
        if(vertical.isPresent()) {
            formData.add(VERTICAL.getInputID(), vertical.get());
        } else {
            formData.add(VERTICAL.getInputID(), OTHER_OPTION);
            formData.add(VERTICAL.getInputID() + OTHER_OPTION_RESPONSE, safe(record, "Vertical"));
        }


        formData.add(REFINISHING_TASK.getInputID(), safe(record, "Refinishing task performed"));

        Optional<String> pv_remark = Arrays.stream(PV_REMARKS_AFTER_REFINISHING).filter(remark -> remark.equals(safe(record, "PV remarks after Refinishing"))).findFirst();
        if(pv_remark.isPresent()) {
            formData.add(PV_REMARK.getInputID(), pv_remark.get());
        } else {
            formData.add(PV_REMARK.getInputID(), OTHER_OPTION);
            formData.add(PV_REMARK.getInputID() + OTHER_OPTION_RESPONSE, safe(record, "PV remarks after Refinishing"));
        }

        return formData;
    }

    // ================= REAL SUBMIT =================
    private void realSubmit(MultiValueMap<String, String> formData) {

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        HttpEntity<MultiValueMap<String, String>> request =
                new HttpEntity<>(formData, headers);

        ResponseEntity<String> response = restTemplate.postForEntity(
                FORM_URL,
                request,
                String.class
        );

        int status = response.getStatusCode().value();

        if (status != 200 && status != 302) {
            throw new RuntimeException("Google Form submit failed. HTTP " + status);
        }
    }

    // ================= MOCK SUBMIT =================
    private void mockSubmit(MultiValueMap<String, String> formData) {

        log.info("🧪 MOCK GOOGLE FORM SUBMIT");

        formData.forEach((key, value) ->
                log.info("  {} = {}", key, value)
        );

        if (Math.random() < 0.05) {
            throw new RuntimeException("Mock simulated failure");
        }

        log.info("✅ Mock submission success");
    }

    private String safe(CSVRecord record, String field) {
        return record.isMapped(field) ? record.get(field).trim() : "";
    }
}
