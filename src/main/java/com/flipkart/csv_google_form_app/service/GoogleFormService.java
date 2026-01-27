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

import static com.flipkart.csv_google_form_app.constants.FormInputIDs.*;

@Service
public class GoogleFormService {

//    private static final String HAR_RC = "Haringhata";

    private static final String PV_PASS = "Pass";
    private static final String PV_FAIL = "Fail";

    private static final String NO_DEFECT = "No Defect";

    private static final String FORM_ID = "1FAIpQLSef89B0j2OGItZtl4wyDZ6ZcO8Y4XLuj1GGr2QZhgFgMuOyPQ";

    private static final String FORM_URL =
            "https://docs.google.com/forms/d/e/" + FORM_ID + "/formResponse";

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

        String pvReason = safe(record, "PV Reason");
        String result = pvReason.equals(NO_DEFECT) ? PV_PASS : PV_FAIL;

        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
//        formData.add(RC_NAME.getInputID(), HAR_RC);
        formData.add(RC_NAME.getInputID(), safe(record, "RC Name"));
        formData.add(WSN.getInputID(), safe(record, "Actual WSN"));
        formData.add(CONSIGNMENT_NO.getInputID(), safe(record, "Consignment ID"));
        formData.add(CASPER.getInputID(), safe(record, "Casper"));
        formData.add(PV_REASON.getInputID(), pvReason);
        formData.add(PV_REMARK.getInputID(), result);

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
