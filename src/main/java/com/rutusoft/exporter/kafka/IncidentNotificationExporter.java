//package com.rutusoft.exporter;
//
//import com.fasterxml.jackson.databind.JsonNode;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import io.camunda.zeebe.exporter.api.Exporter;
//import io.camunda.zeebe.exporter.api.context.Context;
//import io.camunda.zeebe.exporter.api.context.Controller;
//import io.camunda.zeebe.protocol.record.Record;
//import io.camunda.zeebe.protocol.record.ValueType;
//import org.slf4j.Logger;
//import org.springframework.http.HttpEntity;
//import org.springframework.http.HttpHeaders;
//import org.springframework.http.MediaType;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.client.RestTemplate;
//
//
//public class IncidentNotificationExporter implements Exporter {
//    private final ObjectMapper objectMapper = new ObjectMapper();
//    private Logger logger;
////    private Producer<String, String> producer;
////    private String kafkaTopic;
//
//    @Override
//    public void configure(Context context) {
//        logger = context.getLogger();
//        logger.info("Exporter configured");
//    }
//
//    @Override
//    public void open(Controller controller) {
//        logger.info("Incident notification exporter opened kafka");
//    }
//
//    @Override
//    public void close() {
////        if (producer != null) {
////            producer.flush();
////            producer.close();
////        }
//        logger.info("Incident notification exporter closed");
//    }
//
//    @Override
//    public void export(Record<?> record) {
//        try {
//            String json = record.toJson();
//            ValueType valueType = record.getValueType();
//            if (valueType == ValueType.USER_TASK) {
//                JsonNode value = objectMapper.readTree(json).get("value");
//                String bpmnProcessId = value.get("bpmnProcessId") == null ? "" : value.get("bpmnProcessId").asText();
//                if (bpmnProcessId.equals("process_incident_management")) {
//                    logger.info("Record in JSON  for user task: {}", json);
////                    sendToKafka(record.getKey(), json);
//                    logger.info("data push : {}", json);
//                    sendViaRestApi(json);
////                    logger.info("Data sent via REST API successfully.");
//                }
//            }
//        } catch (Exception e) {
//            logger.error("Error during export operation", e);
//        }
//    }
//
////    private void sendToKafka(long key, String data) {
////        try {
////            ProducerRecord<String, String> record = new ProducerRecord<>(kafkaTopic, String.valueOf(key), data);
////            producer.send(record, (metadata, exception) -> {
////                if (exception != null) {
////                    logger.error("Failed to send record to Kafka", exception);
////                } else {
////                    logger.info("Record sent to Kafka: topic={}, offset={}", metadata.topic(), metadata.offset());
////                }
////            });
////        } catch (Exception e) {
////            logger.error("Kafka send failed", e);
////        }
////    }
//
//    private void sendViaRestApi(String data) {
//        logger.info("Sending task data  :{}", data);
//        try {
//            RestTemplate restTemplate = new RestTemplate();
//            String url = "http://host.docker.internal:8085/notification-service/notifications/record";
//            HttpHeaders headers = new HttpHeaders();
//            headers.setContentType(MediaType.APPLICATION_JSON);
//            HttpEntity<String> request = new HttpEntity<>(data, headers);
//
//            ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);
//
//            if (response.getStatusCode().is2xxSuccessful()) {
//                logger.info("REST API call succeeded: {}", response.getBody());
//            } else {
//                logger.error("Failed to send data via REST API. Status code: {}", response.getStatusCode());
//                throw new RuntimeException("REST API call failed.");
//            }
//        } catch (Exception e) {
//            logger.error("Exception occurred while calling REST API", e);
//            throw e;
//        }
//    }
//}
//
//
