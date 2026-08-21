package com.rc.Bank_Service.triage.parser;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Component
public class TriageReportParser {

    private final ObjectMapper objectMapper = new ObjectMapper();

    public static class ParsedRecord {
        private String testName;
        private String className;
        private String status; // PASS, FAIL, SKIPPED
        private String errorMessage;
        private String stackTrace;
        private long durationMs;
        private LocalDateTime timestamp = LocalDateTime.now();

        public ParsedRecord(String testName, String className, String status, String errorMessage, String stackTrace, long durationMs) {
            this.testName = testName;
            this.className = className;
            this.status = status;
            this.errorMessage = errorMessage;
            this.stackTrace = stackTrace;
            this.durationMs = durationMs;
        }

        public String getTestName() { return testName; }
        public String getClassName() { return className; }
        public String getStatus() { return status; }
        public String getErrorMessage() { return errorMessage; }
        public String getStackTrace() { return stackTrace; }
        public long getDurationMs() { return durationMs; }
        public LocalDateTime getTimestamp() { return timestamp; }
    }

    public static class ParsedReport {
        private String suiteName;
        private int totalTests;
        private int passedCount;
        private int failedCount;
        private int skippedCount;
        private long durationMs;
        private List<ParsedRecord> records = new ArrayList<>();

        public ParsedReport(String suiteName, int totalTests, int passedCount, int failedCount, int skippedCount, long durationMs, List<ParsedRecord> records) {
            this.suiteName = suiteName;
            this.totalTests = totalTests;
            this.passedCount = passedCount;
            this.failedCount = failedCount;
            this.skippedCount = skippedCount;
            this.durationMs = durationMs;
            this.records = records;
        }

        public String getSuiteName() { return suiteName; }
        public int getTotalTests() { return totalTests; }
        public int getPassedCount() { return passedCount; }
        public int getFailedCount() { return failedCount; }
        public int getSkippedCount() { return skippedCount; }
        public long getDurationMs() { return durationMs; }
        public List<ParsedRecord> getRecords() { return records; }

        public List<ParsedFailureRecord> getFailures() {
            List<ParsedFailureRecord> list = new ArrayList<>();
            for (ParsedRecord r : records) {
                if ("FAIL".equalsIgnoreCase(r.getStatus())) {
                    list.add(new ParsedFailureRecord(r.getTestName(), r.getClassName(), r.getErrorMessage(), r.getStackTrace(), r.getDurationMs()));
                }
            }
            return list;
        }
    }

    public static class ParsedFailureRecord {
        private String testName;
        private String className;
        private String errorMessage;
        private String stackTrace;
        private long durationMs;
        private LocalDateTime timestamp = LocalDateTime.now();

        public ParsedFailureRecord(String testName, String className, String errorMessage, String stackTrace, long durationMs) {
            this.testName = testName;
            this.className = className;
            this.errorMessage = errorMessage;
            this.stackTrace = stackTrace;
            this.durationMs = durationMs;
        }

        public String getTestName() { return testName; }
        public String getClassName() { return className; }
        public String getErrorMessage() { return errorMessage; }
        public String getStackTrace() { return stackTrace; }
        public long getDurationMs() { return durationMs; }
        public LocalDateTime getTimestamp() { return timestamp; }
    }

    public ParsedReport parseFullJunitXml(String xmlContent, String fallbackSuiteName) {
        List<ParsedRecord> records = new ArrayList<>();
        if (xmlContent == null || xmlContent.isBlank()) {
            return new ParsedReport(fallbackSuiteName, 0, 0, 0, 0, 0, records);
        }

        int total = 0;
        int passed = 0;
        int failed = 0;
        int skipped = 0;
        long totalTimeMs = 0;
        String suiteName = fallbackSuiteName;

        try {
            String cleanXml = xmlContent.trim();
            if (cleanXml.startsWith("\uFEFF")) {
                cleanXml = cleanXml.substring(1).trim();
            }

            int xmlStart = cleanXml.indexOf("<");
            int xmlEnd = cleanXml.lastIndexOf(">");
            if (xmlStart >= 0 && xmlEnd > xmlStart) {
                cleanXml = cleanXml.substring(xmlStart, xmlEnd + 1);
            }

            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(false);
            factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", false);
            factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(new ByteArrayInputStream(cleanXml.getBytes(StandardCharsets.UTF_8)));

            NodeList testsuiteList = doc.getElementsByTagName("testsuite");
            if (testsuiteList.getLength() > 0) {
                Element ts = (Element) testsuiteList.item(0);
                String attrName = ts.getAttribute("name");
                if (attrName != null && !attrName.isBlank()) suiteName = attrName;
            }

            NodeList testcaseList = doc.getElementsByTagName("testcase");
            total = testcaseList.getLength();

            for (int i = 0; i < testcaseList.getLength(); i++) {
                Element tc = (Element) testcaseList.item(i);
                String name = tc.getAttribute("name");
                String classname = tc.getAttribute("classname");
                if (classname == null || classname.isBlank()) classname = "AutomatedTestSuite";
                String timeAttr = tc.getAttribute("time");
                long timeMs = 0;
                try {
                    if (timeAttr != null && !timeAttr.isBlank()) {
                        timeMs = (long) (Double.parseDouble(timeAttr) * 1000);
                        totalTimeMs += timeMs;
                    }
                } catch (Exception ignored) {}

                NodeList failureNodes = tc.getElementsByTagName("failure");
                if (failureNodes.getLength() == 0) {
                    failureNodes = tc.getElementsByTagName("error");
                }
                NodeList skippedNodes = tc.getElementsByTagName("skipped");

                if (failureNodes.getLength() > 0) {
                    failed++;
                    Element failureEl = (Element) failureNodes.item(0);
                    String message = failureEl.getAttribute("message");
                    if (message == null || message.isBlank()) message = failureEl.getAttribute("type");
                    if (message == null || message.isBlank()) message = failureEl.getTextContent();
                    if (message == null || message.isBlank()) message = "Assertion or Test Execution Failure";
                    String stackTrace = failureEl.getTextContent();
                    records.add(new ParsedRecord(name, classname, "FAIL", message, stackTrace, timeMs));
                } else if (skippedNodes.getLength() > 0) {
                    skipped++;
                    records.add(new ParsedRecord(name, classname, "SKIPPED", "Test Execution Skipped", "", timeMs));
                } else {
                    passed++;
                    records.add(new ParsedRecord(name, classname, "PASS", null, null, timeMs));
                }
            }
        } catch (Exception e) {
            System.err.println("[TriageReportParser] Error parsing JUnit XML: " + e.getMessage());
        }

        return new ParsedReport(suiteName, total, passed, failed, skipped, totalTimeMs, records);
    }

    public List<ParsedFailureRecord> parseJunitXml(String xmlContent) {
        return parseFullJunitXml(xmlContent, "JUnit Suite").getFailures();
    }

    public List<ParsedFailureRecord> parseNewmanJson(String jsonContent) {
        List<ParsedFailureRecord> failures = new ArrayList<>();
        if (jsonContent == null || jsonContent.isBlank()) return failures;

        try {
            JsonNode root = objectMapper.readTree(jsonContent);
            JsonNode executions = root.path("run").path("executions");
            for (JsonNode exec : executions) {
                String testName = exec.path("item").path("name").asText("API Test");
                JsonNode assertions = exec.path("assertions");
                for (JsonNode assertion : assertions) {
                    if (assertion.has("error")) {
                        String errorMsg = assertion.path("error").path("message").asText("Assertion Failed");
                        String stack = assertion.path("error").path("stack").asText(errorMsg);
                        failures.add(new ParsedFailureRecord(testName, "NewmanApiTestSuite", errorMsg, stack, 150));
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("[TriageReportParser] Error parsing Newman JSON: " + e.getMessage());
        }
        return failures;
    }
}
