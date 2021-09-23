
resource "aws_ecs_cluster" "cluster" {
  name = "${var.prefix}-cluster-${var.flavour}"

  tags = {
    Name = "${var.prefix}-ecs-builder"
    stack   = var.stack
    prefix  = var.prefix
    flavour = var.flavour
  }
}

resource "aws_iam_role" "task_run_role" {
  name = "${var.prefix}_builder_run_${var.flavour}"

  assume_role_policy = <<EOF
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Sid": "",
      "Effect": "Allow",
      "Principal": {
        "Service": "ecs-tasks.amazonaws.com"
      },
      "Action": "sts:AssumeRole"
    }
  ]
}
EOF
}

resource "aws_iam_role" "task_exec_role" {
  name = "${var.prefix}_builder_exec_${var.flavour}"

  assume_role_policy = <<EOF
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Sid": "",
      "Effect": "Allow",
      "Principal": {
        "Service": "ecs-tasks.amazonaws.com"
      },
      "Action": "sts:AssumeRole"
    }
  ]
}
EOF
}

data "aws_iam_policy_document" "task_run_role_policy" {
  statement {
    actions = [
      "s3:ListBucket",
      "s3:GetObject",
      "s3:PutObject",
    ]

    resources = [
      "arn:aws:s3:::${var.s3_bucket_name}",
      "arn:aws:s3:::${var.s3_bucket_name}/*",
    ]

    effect = "Allow"
  }
}

resource "aws_iam_role_policy" "task_run_role_policy" {
  name = "${var.prefix}_builder_run_${var.flavour}"
  role = aws_iam_role.task_run_role.id

  policy = data.aws_iam_policy_document.task_run_role_policy.json
}

resource "aws_iam_role_policy" "task_exec_role_policy" {
  name = "${var.prefix}_builder_exec_${var.flavour}"
  role = aws_iam_role.task_exec_role.id

  policy = data.aws_iam_policy_document.task_exec_role_policy.json
}

data "aws_iam_policy_document" "task_exec_role_policy" {
  statement {
    effect    = "Allow"
    actions   = [
      "ecr:GetAuthorizationToken",
      "ecr:BatchCheckLayerAvailability",
      "ecr:GetDownloadUrlForLayer",
      "ecr:BatchGetImage",
      "logs:CreateLogStream",
      "logs:PutLogEvents"
    ]
    resources = ["*"]
  }
}

locals {
  target_logs = "/aws/fargate/${var.prefix}-builder-${var.flavour}"
}

resource aws_cloudwatch_log_group logs {
  name = local.target_logs
  retention_in_days = 60
}

resource "aws_ecs_task_definition" "builder" {
  family                   = "${var.prefix}-builder-${var.flavour}"
  task_role_arn            = aws_iam_role.task_run_role.arn
  execution_role_arn       = aws_iam_role.task_exec_role.arn
  network_mode             = "awsvpc"
  requires_compatibilities = ["FARGATE"]
  cpu                      = "2048"
  memory                   = "4096"
  container_definitions = <<DEFINITION
[
  {
    "name": "builder",
    "image": "${aws_ecr_repository.ecr.repository_url}:${local.image_tag}",
    "essential": true,
    "logConfiguration": {
        "logDriver": "awslogs",
        "options": {
          "awslogs-group": "${local.target_logs}",
          "awslogs-region": "${data.aws_region.aws.name}",
          "awslogs-stream-prefix": "/aws/fargate"
        }
    },
    "environment": [
        {"name": "KTJS_BUCKET", "value": "${var.s3_bucket_name}"},
        {"name": "KTJS_CDN_BASE", "value": "${var.static_cdn_url_base}"}
    ]
  }
]
DEFINITION

  tags = {
    stack   = var.stack
    prefix  = var.prefix
    flavour = var.flavour
  }
}

output "ecs_cluster_name" {
  value = aws_ecs_cluster.cluster.name
}

output "ecs_task_definition_arn" {
  value = aws_ecs_task_definition.builder.arn
}

output "ecs_task_subnets" {
  value = join(",", aws_default_subnet.subnet.*.id)
}

data "aws_availability_zones" "az" {
}

resource "aws_default_subnet" "subnet" {
  count = length(data.aws_availability_zones.az.names)
  availability_zone = data.aws_availability_zones.az.names[count.index]
}
