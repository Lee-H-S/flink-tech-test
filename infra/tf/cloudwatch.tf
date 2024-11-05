resource "aws_cloudwatch_log_group" "flink_log_group" {
  name              = "/flink/application-logs"
  retention_in_days = 14
}