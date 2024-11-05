# EC2 Instance for Apache Flink
// TODO: Consider autoscaling, if the load grows substantially: move to EMR or Managed Flink
resource "aws_instance" "flink_instance" {
  ami           = "ami-047bb4163c506cd98" # Amazon Linux 2 AMI (verify region-specific AMI)
  instance_type = var.instance_type
  key_name      = var.key_pair_name
  iam_instance_profile = aws_iam_instance_profile.flink_instance_profile.name
  security_groups      = [aws_security_group.flink_sg.name]


  # User data script to set up Flink and fetch the JAR from S3
  // TODO: Verify this works
  user_data = <<-EOF
              #!/bin/bash
              # Install Java 11 and AWS CLI
              yum update -y
              amazon-linux-extras install java-openjdk11 -y
              curl "https://awscli.amazonaws.com/awscli-exe-linux-x86_64.zip" -o "awscliv2.zip"
              unzip awscliv2.zip
              sudo ./aws/install

              # Download and install Apache Flink 1.14.6
              wget https://archive.apache.org/dist/flink/flink-1.14.6/flink-1.14.6-bin-scala_2.12.tgz
              tar -xzf flink-1.14.6-bin-scala_2.12.tgz
              mv flink-1.14.6 /opt/flink

              # Configure Flink for S3 checkpointing and CloudWatch logging
              echo "state.backend: filesystem" >> /opt/flink/conf/flink-conf.yaml
              echo "state.checkpoints.dir: s3://${var.s3_bucket_name}/checkpoints" >> /opt/flink/conf/flink-conf.yaml
              echo "metrics.reporter.cloudwatch.class: org.apache.flink.metrics.cloudwatch.CloudWatchReporter" >> /opt/flink/conf/flink-conf.yaml
              echo "metrics.reporter.cloudwatch.region: ${var.region}" >> /opt/flink/conf/flink-conf.yaml
              echo "metrics.reporter.cloudwatch.namespace: FlinkMetrics" >> /opt/flink/conf/flink-conf.yaml

              # Fetch Flink job JAR from S3
              aws s3 cp s3://${var.s3_bucket_name}/${var.s3_jar_key} /opt/flink/lib/job.jar

              # Start Flink
              /opt/flink/bin/start-cluster.sh

              # Run the Flink job
              /opt/flink/bin/flink run -d -c PageviewAggregationSinkApp /opt/flink/lib/job.jar
              EOF

  tags = {
    Name = "${var.flink_application_name}-ec2"
  }
}

# IAM Instance Profile for EC2 Role
resource "aws_iam_instance_profile" "flink_instance_profile" {
  name = "${var.flink_application_name}-profile"
  role = aws_iam_role.flink_ec2_role.name
}
