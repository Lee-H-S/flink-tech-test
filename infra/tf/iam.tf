# IAM Role and Policy for EC2 Instance
resource "aws_iam_role" "flink_ec2_role" {
  name = "${var.flink_application_name}-role"
  assume_role_policy = jsonencode({
    Version = "2012-10-17",
    Statement = [
      {
        Action = "sts:AssumeRole",
        Effect = "Allow",
        Principal = {
          Service = "ec2.amazonaws.com"
        }
      }
    ]
  })
}

//TODO: Update for least privilege
resource "aws_iam_role_policy" "flink_policy" {
  name   = "${var.flink_application_name}-policy"
  role   = aws_iam_role.flink_ec2_role.id
  policy = jsonencode({
    Version = "2012-10-17",
    Statement = [
      {
        Action   = ["s3:GetObject", "s3:ListBucket", "s3:PutObject"],
        Effect   = "Allow",
        Resource = [
          aws_s3_bucket.flink_bucket.arn,
          "${aws_s3_bucket.flink_bucket.arn}/*"
        ]
      },
      {
        Action = ["logs:CreateLogGroup", "logs:CreateLogStream", "logs:PutLogEvents"],
        Effect = "Allow",
        Resource = [
          aws_cloudwatch_log_group.flink_log_group.arn,
          "${aws_cloudwatch_log_group.flink_log_group.arn}:*"
        ]
      },
      {
        Action = ["cloudwatch:PutMetricData"],
        Effect = "Allow",
        Resource = "*"
      }
    ]
  })
}
