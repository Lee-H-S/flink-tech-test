output "flink_ec2_instance_id" {
  value = aws_instance.flink_instance.id
}

output "flink_ec2_public_ip" {
  value = aws_instance.flink_instance.public_ip
}

output "s3_bucket_name" {
  value = aws_s3_bucket.flink_bucket.bucket
}
