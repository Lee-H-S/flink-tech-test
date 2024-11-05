import org.apache.flink.streaming.api.scala.{DataStream, StreamExecutionEnvironment}
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer
import org.apache.flink.api.common.serialization.SimpleStringEncoder
import org.apache.flink.core.fs.Path
import org.apache.flink.streaming.api.scala._
import org.apache.flink.api.common.eventtime.WatermarkStrategy
import org.apache.flink.connector.file.sink.FileSink
import aws.S3Utils._

import java.time.Duration
import spray.json._
import config.FlinkConfigLoader.FlinkConfig
import config.S3ConfigLoader.S3Config
import flink.CustomBucketAssigner
import model.Pageview
import org.apache.flink.connector.kafka.source.KafkaSource
import org.apache.flink.streaming.api.datastream.DataStreamSink
import org.apache.flink.streaming.api.functions.sink.filesystem.rollingpolicies.DefaultRollingPolicy
import serdes.{PageviewJsonProtocol, PageviewTimestampAssigner}

class Flow(env: StreamExecutionEnvironment, kafkaConsumer:  KafkaSource[String], s3Config: S3Config, flinkConfig: FlinkConfig) {

  def run(): DataStreamSink[String] = {
    implicit val aggregatedFormat = PageviewJsonProtocol.aggregatedPageviewFormat

    val pageViewStream = setupStream()

    val aggregatedStream = Aggregation.aggregateStream(pageViewStream, flinkConfig.windowSizeMinutes)

    val s3Sink = setupS3Sink()

    aggregatedStream
      .map(_.toJson.toString)
      .sinkTo(s3Sink)
  }

  private def setupStream(): DataStream[Pageview] = {
    implicit val pageviewFormat = PageviewJsonProtocol.pageviewFormat

    // TODO: Come up with Watermark Strategy
    env
      .fromSource(kafkaConsumer, WatermarkStrategy.noWatermarks(), "Kafka Source")
      .map(_.parseJson.convertTo[Pageview])
      .assignTimestampsAndWatermarks(
        WatermarkStrategy
          .forBoundedOutOfOrderness[Pageview](Duration.ofSeconds(10))
          .withTimestampAssigner(new PageviewTimestampAssigner)
      )
  }

  private def setupS3Sink(): FileSink[String] = {
    // TODO: Write to more performant file format - dependent on use-case downstream
    FileSink.forRowFormat(
        new Path(s"s3://${joinWithSlash(s3Config.bucketName, s3Config.outputPath)}"),
        new SimpleStringEncoder[String]("UTF-8")
      )
      .withRollingPolicy(
        DefaultRollingPolicy.builder()
          .withRolloverInterval(Duration.ofMinutes(s3Config.fileRolloverIntervalMinutes).toMillis) // Roll file every 15 minutes
          .withInactivityInterval(Duration.ofMinutes(s3Config.inactivityIntervalMinutes).toMillis) // Roll if no new data for 5 minutes
          .withMaxPartSize(1024 * 1024 * s3Config.maxPartSizeMB) // 400 MB per file
          .build()
      )
      .withBucketAssigner(new CustomBucketAssigner)
      .build()
  }
}
