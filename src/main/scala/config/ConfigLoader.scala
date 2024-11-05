package config

import com.typesafe.config.{Config, ConfigFactory}

object ConfigLoader {
  private val config: Config = ConfigFactory.load()
}


object FlinkConfigLoader {

  case class FlinkConfig(parallelism: Int, watermarkOutOfOrderness: Long, windowSizeMinutes: Int)

  def apply(config: Config) = {

    val flinkConfig = config.getConfig("flink")

    FlinkConfig(
    parallelism = flinkConfig.getInt("parallelism"),
    watermarkOutOfOrderness = flinkConfig.getLong("watermark-out-of-orderness-seconds"),
    windowSizeMinutes = flinkConfig.getInt("window-size-minutes")
    )
  }
}

object S3ConfigLoader {

  case class S3Config(bucketName: String, outputPath: String, fileRolloverIntervalMinutes: Long,
                      inactivityIntervalMinutes: Long, maxPartSizeMB: Long)

  def apply(config: Config) = {

    val s3Config = config.getConfig("s3")
    S3Config(
    bucketName = s3Config.getString("bucket-name"),
    outputPath = s3Config.getString("output-path"),
    fileRolloverIntervalMinutes = s3Config.getLong("file-rollover-interval-minutes"),
    inactivityIntervalMinutes = s3Config.getLong("inactivity-interval-minutes"),
    maxPartSizeMB = s3Config.getLong("max-part-size-mb")
    )
  }
}

object KafkaConfigLoader {

  case class KafkaConfig(bootstrapServers: String, groupId: String, inputTopic: String)


  def apply(config: Config) = {
    val kafkaConfig = config.getConfig("kafka")
    KafkaConfig(
    bootstrapServers = kafkaConfig.getString("bootstrap-servers"),
    groupId = kafkaConfig.getString("group-id"),
    inputTopic = kafkaConfig.getString("input-topic")
    )
  }
}
