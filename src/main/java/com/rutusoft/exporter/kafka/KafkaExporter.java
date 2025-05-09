package com.rutusoft.exporter.kafka;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.camunda.zeebe.exporter.api.Exporter;
import io.camunda.zeebe.exporter.api.context.Context;
import io.camunda.zeebe.exporter.api.context.Controller;
import io.camunda.zeebe.protocol.record.Record;
import io.camunda.zeebe.protocol.record.ValueType;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;

public class KafkaExporter implements Exporter {
    private final Logger logger = LoggerFactory.getLogger(KafkaExporter.class);
    private final ObjectMapper objectMapper = new ObjectMapper();
    private KafkaProducer<String, String> producer;
    private String topic;
    private String processFilter;


    @Override
    public void configure(Context context) throws Exception {
        Exporter.super.configure(context);

        // Hardcoded Kafka config
        String brokers = "host.docker.internal:9092";
        topic = "ims-task";
        processFilter = "process_incident_management";

        // Kafka producer properties
        Properties props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, brokers);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        props.put(ProducerConfig.ACKS_CONFIG, "1");
        props.put(ProducerConfig.RETRIES_CONFIG, 1);
        props.put(ProducerConfig.LINGER_MS_CONFIG, 5);

        producer = new KafkaProducer<>(props);

        logger.info("✅ KafkaExporter configured with hardcoded settings: brokers={} topic={} filter={}", brokers, topic, processFilter);
    }

    @Override
    public void open(Controller controller) {
        logger.info("🚀 KafkaExporter opened ");
        Exporter.super.open(controller);

    }

    @Override
    public void close() {
        if (producer != null) {
            producer.flush();
            producer.close();
            logger.info("🛑 KafkaProducer closed");
        }

        Exporter.super.close();

    }

    @Override
    public void export(Record<?> record) {
        logger.info("record : {}", record);
        try {
            if (record.getValueType() != ValueType.USER_TASK) {
                return;
            }
            logger.info(" filter usertask  : {}", record.getRecordType());

            final String json = record.toJson();
            final JsonNode valueNode = objectMapper.readTree(json).path("value");
            final String bpmnProcessId = valueNode.path("bpmnProcessId").asText("");

            if (!processFilter.equals(bpmnProcessId)) {
                return;
            }
            logger.info("filter process  : {}", record.getRecordType());


            final String key = String.valueOf(record.getKey());
            final ProducerRecord<String, String> kafkaRec =
                    new ProducerRecord<>(topic, key, json);
            logger.info("kafka record prepared  : {}", kafkaRec);

            producer.send(kafkaRec, (meta, ex) -> {
                if (ex != null) {
                    logger.error("❌ Failed to send record key={} to Kafka", key, ex);
                } else {
                    logger.info("📤 Sent key={} to {} [partition={}, offset={}]",
                            key, meta.topic(), meta.partition(), meta.offset());
                }
            });
            logger.info("kafka send : {}", json);
        } catch (Exception e) {
            logger.error("⚠️ Error in KafkaExporter.export()", e);
        }

    }
}