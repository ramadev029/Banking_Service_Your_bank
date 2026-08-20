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

    public List<ParsedFailureRecord> parseJunitXml(String xmlContent) {
        List<ParsedFailureRecord> failures = new ArrayList<>();
        if (xmlContent == null || xmlContent.isBlank()) return failures;

        try {
            String cleanXml = xmlContent.trim();
            // Strip UTF-8 Byte Order Mark (BOM) \uFEFF if present from PowerShell
            if (cleanXml.startsWith("\uFEFF")) {
                cleanXml = cleanXml.substring(1).trim();
            }

            // Slice precisely from first '<' to last '>' to remove any trailing JSON quotes or formatting
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

            NodeList testcaseList = doc.getElementsByTagName("testcase");
            for (int i = 0; i < testcaseList.getLength(); i++) {
                Element tc = (Element) testcaseList.item(i);
                String name = tc.getAttribute("name");
                String classname = tc.getAttribute("classname");
                String timeAttr = tc.getAttribute("time");
                long timeMs = 0;
                try {
                    if (timeAttr != null && !timeAttr.isBlank()) {
                        timeMs = (long) (Double.parseDouble(timeAttr) * 1000);
                    }
                } catch (Exception ignored) {}

                NodeList failureNodes = tc.getElementsByTagName("failure");
                if (failureNodes.getLength() == 0) {
                    failureNodes = tc.getElementsByTagName("error");
                }

                if (failureNodes.getLength() > 0) {
                    Element failureEl = (Element) failureNodes.item(0);
                    String message = failureEl.getAttribute("message");
                    if (message == null || message.isBlank()) {
                        message = failureEl.getAttribute("type");
                    }
                    if (message == null || message.isBlank()) {
                        message = failureEl.getTextContent();
                    }
                    if (message == null || message.isBlank()) {
                        message = "Assertion or Test Execution Failure";
                    }
                    String stackTrace = failureEl.getTextContent();
                    failures.add(new ParsedFailureRecord(name, classname, message, stackTrace, timeMs));
                }
            }
        } catch (Exception e) {
            System.err.println("[TriageReportParser] Error parsing JUnit XML: " + e.getMessage());
            e.printStackTrace();
        }
        return failures;
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
