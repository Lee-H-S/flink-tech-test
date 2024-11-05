variable "region" {
  description = "The AWS region to deploy resources in"
  type        = string
  default     = "eu-west-1"
}

variable "instance_type" {
  description = "The EC2 instance type for running Flink"
  type        = string
  default     = "t3.medium"
}

variable "environment" {
  description = "The AWS Environment the application will be running in"
  type        = string
}

variable "flink_application_name" {
  description = "The name of the Flink application"
  type        = string
  default     = "my-pageview-aggregation-app"
}

variable "s3_bucket_name" {
  description = "S3 bucket name for the Flink JAR and checkpoints"
  type        = string
}

variable "s3_jar_key" {
  description = "S3 object key for the Flink job JAR"
  type        = string
}

variable "key_pair_name" {
  description = "The name of the SSH key pair for accessing the EC2 instance"
  type        = string
}
