package utils

import com.typesafe.config.{Config, ConfigFactory}

object ConfigLoader {
  private val config: Config = ConfigFactory.load()

  object Kafka {
    val bootstrapServers: String = sys.env.getOrElse("KAFKA_BOOTSTRAP_SERVERS", config.getString("kafka.bootstrap-servers"))
    val groupId: String = sys.env.getOrElse("KAFKA_GROUP_ID", config.getString("kafka.group-id"))
    val inputTopic: String = sys.env.getOrElse("KAFKA_INPUT_TOPIC", config.getString("kafka.input-topic"))
  }

  object S3 {
    val bucketName: String = sys.env.getOrElse("S3_BUCKET_NAME", config.getString("s3.bucket-name"))
    val endpointUrl: String = sys.env.getOrElse("S3_ENDPOINT_URL", "http://localstack:4566")  // Set Localstack URL for Docker
  }
}

