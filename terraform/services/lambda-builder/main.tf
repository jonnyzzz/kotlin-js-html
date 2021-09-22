variable "prefix" {}
variable "stack" {}
variable "s3_bucket_name" {}
variable "flavour" {}

locals {
  lambda_dir = abspath("${path.module}/../../../aws-builder-lambda")
  include_file = abspath("${local.lambda_dir}/generated_variables.sh")
  image_tag = "PROD"
}

data "aws_iam_policy_document" "iam" {
  statement {
    actions = ["sts:AssumeRole"]
    principals {
      identifiers = ["lambda.amazonaws.com"]
      type        = "Service"
    }
    effect  = "Allow"
  }
}

resource "aws_ecr_repository" "ecr" {
  name = "${var.stack}-builder-${var.flavour}"
  image_scanning_configuration {
    scan_on_push = true
  }
  image_tag_mutability = "MUTABLE"
  tags = {
    stack   = var.stack
    prefix  = var.prefix
    flavour = var.flavour
  }
}

data "aws_region" "aws" {

}

resource "local_file" "ecr" {
  filename = local.include_file
  content = <<EOT

AWS_REGION=${data.aws_region.aws.name}
ECR_URL=${aws_ecr_repository.ecr.repository_url}
ECR_TAG=${local.image_tag}

EOT
}

resource "aws_ecr_repository_policy" "ecr" {
  policy     = data.aws_iam_policy_document.iam.json
  repository = aws_ecr_repository.ecr.id
}

data "aws_iam_policy_document" "ecr" {
  statement {
    effect  = "Allow"
    actions = [
      "ecr:BatchGetImage",
      "ecr:GetDownloadUrlForLayer"
    ]
    principals {
      identifiers = ["lambda.amazonaws.com"]
      type        = "Service"
    }
  }
}

data "aws_iam_policy_document" "permissions" {
  statement {
    actions = [
      "s3:ListBucket",
      "s3:GetObject",
    ]

    resources = [
      "arn:aws:s3:::${var.s3_bucket_name}",
      "arn:aws:s3:::${var.s3_bucket_name}/*",
    ]

    effect = "Allow"
  }
}

resource "aws_iam_role" "iam" {
  name               = "${var.prefix}_iam_builder_${var.flavour}"
  assume_role_policy = data.aws_iam_policy_document.iam.json
}

resource "aws_iam_role_policy" "iam" {
  role   = aws_iam_role.iam.id
  name   = "${var.prefix}_build_lambda_policy_${var.flavour}"
  policy = data.aws_iam_policy_document.permissions.json
}

resource "aws_lambda_function" "f" {
  function_name = "${var.prefix}_build_lambda_${var.flavour}"
  role          = aws_iam_role.iam.arn

  image_uri = "${aws_ecr_repository.ecr.repository_url}:${local.image_tag}"
  package_type  = "Image"

  timeout     = "600"
  memory_size = "2048"

  environment {
    variables = {
      KTJS_BUCKET = var.s3_bucket_name
    }
  }
}

module "cloudwatch" {
  source = "../util-lambda-cloudwatch"

  lambda_function_name = aws_lambda_function.f.function_name
  lambda_iam_role_id   = aws_iam_role.iam.id
}
