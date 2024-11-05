# S3 Bucket for Flink JAR and Checkpoints

// TODO: Block public access, add lifecycle, etc.
resource "aws_s3_bucket" "flink_bucket" {
  bucket = "${var.s3_bucket_name}-${var.environment}"

  lifecycle {
    prevent_destroy = true
  }
}