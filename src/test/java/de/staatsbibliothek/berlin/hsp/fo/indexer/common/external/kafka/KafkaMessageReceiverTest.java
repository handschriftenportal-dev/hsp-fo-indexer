package de.staatsbibliothek.berlin.hsp.fo.indexer.common.external.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.staatsbibliothek.berlin.hsp.fo.indexer.BaseIntegrationTest;
import de.staatsbibliothek.berlin.hsp.fo.indexer.api.IndexerHealthIndicator;
import de.staatsbibliothek.berlin.hsp.fo.indexer.common.exception.HSPException;
import de.staatsbibliothek.berlin.hsp.fo.indexer.testutil.ActivityMessageHelper;
import de.staatsbibliothek.berlin.hsp.messaging.activitystreams.impl.model.ActivityStreamMessage;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;

@EmbeddedKafka
@ExtendWith(SpringExtension.class)
@ContextConfiguration( classes = {ObjectMapper.class})
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestPropertySource(properties = {
        "kafka.bootstrap-servers=http://${spring.embedded.kafka.brokers}",
        "kafka.groupid=hsp-fo-indexer-test",
        "common.restart-interval=1"
}, locations = "classpath:application-integration.yml")
public class KafkaMessageReceiverTest extends BaseIntegrationTest {

  private static final String TOPIC_NAME = "test-topic";
  Resource kodRes;
  Resource descRes;
  private Producer<String, String> producer;
  @Value("${kafka.groupid}")
  private String kafkaGroupId;

  @Value("${kafka.bootstrap-servers}")
  private String kafkaBootStrapServers;

  @Autowired
  private EmbeddedKafkaBroker embeddedKafkaBroker;

  @Autowired
  private ObjectMapper objectMapper;

  @Mock
  private SolrKafkaMessageHandler solrKafkaMessageHandler;

  private KafkaMessageReceiver messageReceiverSpy;

  public KafkaMessageReceiverTest() {
    ResourceLoader resourceLoader = new DefaultResourceLoader();
    kodRes = resourceLoader.getResource("fixtures/loremIpsum_kod.xml");
    descRes = resourceLoader.getResource("fixtures/loremIpsum_beschreibung.xml");
  }

  @BeforeAll
  void setUp() {
    Map<String, Object> configs = new HashMap<>(KafkaTestUtils.producerProps(embeddedKafkaBroker));
    producer = new DefaultKafkaProducerFactory<>(configs, new StringSerializer(), new StringSerializer()).createProducer();
    KafkaMessageReceiver kafkaMessageReceiver = new KafkaMessageReceiver(kafkaBootStrapServers, kafkaGroupId);
    kafkaMessageReceiver.setKafkaMessageHandler(solrKafkaMessageHandler);
    kafkaMessageReceiver.setTopicName(TOPIC_NAME);
    messageReceiverSpy = Mockito.spy(kafkaMessageReceiver);
  }

  @AfterAll
  void shutdown() {
    producer.close();
  }

  @Test
  void whenExceptionIsThrown_thenReceiverStopsConsuming() throws Exception {
    sendMessage();
    IndexerHealthIndicator.setCriticalException(new HSPException("error"));

    messageReceiverSpy.start();

    verify(messageReceiverSpy, timeout(1000).times(1)).stop();
  }

  private void sendMessage() throws Exception{
    final ActivityStreamMessage asm = ActivityMessageHelper.fromResource(kodRes, descRes);
    String message = objectMapper.writeValueAsString(asm);
    producer.send(new ProducerRecord<>(TOPIC_NAME, 0, asm.getId(), message));
    producer.flush();
  }
}
