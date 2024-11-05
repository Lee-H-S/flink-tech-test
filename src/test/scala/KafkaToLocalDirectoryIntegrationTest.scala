import aws.S3Utils.joinWithSlash
import com.typesafe.scalalogging.StrictLogging
import io.github.embeddedkafka.Codecs.stringDeserializer
import io.github.embeddedkafka.{EmbeddedKafka, EmbeddedKafkaConfig}
import org.apache.flink.api.common.eventtime.WatermarkStrategy
import org.apache.flink.api.common.serialization.{SimpleStringEncoder, SimpleStringSchema}
import org.apache.flink.streaming.api.scala._
import org.apache.flink.connector.file.sink.FileSink
import org.apache.flink.connector.kafka.source.KafkaSource
import org.apache.flink.connector.kafka.source.enumerator.initializer.OffsetsInitializer
import org.apache.flink.core.fs.Path
import org.apache.flink.streaming.api.functions.sink.filesystem.rollingpolicies.DefaultRollingPolicy
import org.scalatest.BeforeAndAfterAll
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.must
import org.scalatest.matchers.must.Matchers.be
import org.scalatest.matchers.should.Matchers
import utils.FileUtils

import java.nio.file.Files
import java.time.Duration
import java.util.Properties
import scala.io.Source
import scala.jdk.CollectionConverters.asScalaIteratorConverter

class KafkaToLocalDirectoryIntegrationTest extends AnyFlatSpec
  with Matchers
  with EmbeddedKafka
  with BeforeAndAfterAll
  with StrictLogging {

  implicit val kafkaConfig: EmbeddedKafkaConfig = EmbeddedKafkaConfig(
    kafkaPort = 9092,
    zooKeeperPort = 2181,
    customBrokerProperties = Map("auto.create.topics.enable" -> "true")
  )

  val topicName = "pageviews"

  // TODO: Make this write to a mock S3
  "Flink Kafka to S3 pipeline" should "read data from Kafka and write to a local directory" in {
    // Start Embedded Kafka
    withRunningKafka {

      createCustomTopic(topicName)

      Thread.sleep(500)

      val tempDir = Files.createTempDirectory("flink-local-output")

      val env = StreamExecutionEnvironment.getExecutionEnvironment
      env.setParallelism(1)

      publishStringMessageToKafka(topicName, """{"user_id": 1234, "postcode": "SW19", "webpage": "www.website.com/index.html", "timestamp": 1611662684}""")
      publishStringMessageToKafka(topicName, """{"user_id": 1235, "postcode": "SW19", "webpage": "www.website.com/contact.html", "timestamp": 1611662740}""")
      publishStringMessageToKafka(topicName, """{"user_id": 1236, "postcode": "SW20", "webpage": "www.website.com/services.html", "timestamp": 1611662741}""")

      // Set up the Kafka source
      val source: KafkaSource[String] = KafkaSource.builder()
        .setBootstrapServers("localhost:9092")
        .setTopics(topicName)
        .setGroupId("test-group")
        .setStartingOffsets(OffsetsInitializer.earliest())
        .setValueOnlyDeserializer(new SimpleStringSchema())
        .build();

      val kafkaStream = env.fromSource(source, WatermarkStrategy.noWatermarks(), "Kafka Source")

      // Configure FileSink to write to the temporary directory
      val fileSink = FileSink.forRowFormat(
          new Path(tempDir.toUri.toString),
          new SimpleStringEncoder[String]("UTF-8")
        )
        .withRollingPolicy(
          DefaultRollingPolicy.builder()
            .withRolloverInterval(Duration.ofSeconds(5).toMillis) // Trigger rollover every 5 seconds
            .withInactivityInterval(Duration.ofSeconds(5).toMillis) // Finalize files after 5 seconds of inactivity
            .withMaxPartSize(1024 * 1024) // 1 MB part size for testing
            .build()
        )
        .build()

      // Process the Kafka stream and write to S3
      kafkaStream.sinkTo(fileSink)
      env.executeAsync("Kafka to Local Directory Test")

      Thread.sleep(10000)

      // Verify output in the temporary directory
      val outputFiles = Files.list(tempDir).iterator().asScala.toList

      outputFiles should not be empty

      outputFiles.filter(Files.isRegularFile(_)).foreach { file =>
        println(s"Verifying file: $file")
        val content = Source.fromFile(file.toFile).getLines().mkString("\n")
        println(s"File content: $content")
        content should include ("message")
      }

      // Clean up the temporary directory after test completion
      FileUtils.deleteRecursively(tempDir)
    }
  }
}
