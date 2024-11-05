import org.apache.flink.api.common.serialization.{SimpleStringEncoder, SimpleStringSchema}
import org.apache.flink.core.fs.Path
import org.apache.flink.streaming.api.scala._
import org.apache.flink.streaming.api.windowing.time.Time
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer
import org.apache.flink.api.common.eventtime.{SerializableTimestampAssigner, WatermarkStrategy}
import org.apache.flink.connector.file.sink.FileSink

import java.util.Properties
import java.time.Duration
import spray.json._
import DefaultJsonProtocol._
import com.typesafe.config.ConfigFactory
import config.FlinkConfigLoader.FlinkConfig
import config.{FlinkConfigLoader, KafkaConfigLoader, S3ConfigLoader}
import org.apache.flink.connector.kafka.source.KafkaSource
import org.apache.flink.connector.kafka.source.enumerator.initializer.OffsetsInitializer
import org.apache.flink.core.io.SimpleVersionedSerializer
import org.apache.flink.streaming.api.TimeCharacteristic
import org.apache.flink.streaming.api.functions.sink.filesystem.BucketAssigner
import org.apache.flink.streaming.api.functions.sink.filesystem.bucketassigners.SimpleVersionedStringSerializer
import org.apache.flink.streaming.api.functions.sink.filesystem.rollingpolicies.DefaultRollingPolicy

import java.time.format.DateTimeFormatter
import java.util.Properties
import java.time.ZoneId
import java.util.Locale

object PageviewAggregationSinkApp {

  //TODO: Add Logger & Cloudwatch Metrics Dependency
  def main(args: Array[String]): Unit = {

    // Load configurations
    val config = ConfigFactory.load()

    val flinkConfig = FlinkConfigLoader(config)
    val kafkaConfig = KafkaConfigLoader(config)
    val s3Config    = S3ConfigLoader(config)

    // Set up the execution environment
    val env = StreamExecutionEnvironment.getExecutionEnvironment
    env.setParallelism(flinkConfig.parallelism)

    // Kafka Source
    val source: KafkaSource[String] = KafkaSource.builder()
      .setBootstrapServers(kafkaConfig.bootstrapServers)
      .setTopics(kafkaConfig.inputTopic)
      .setGroupId(kafkaConfig.groupId)
      .setStartingOffsets(OffsetsInitializer.earliest())
      .setValueOnlyDeserializer(new SimpleStringSchema())
      .build();

    new Flow(env, source, s3Config, flinkConfig).run()

    // Execute the Flink pipeline
    env.execute("Pageview Aggregation Job")
  }
}
