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

  description = "Runs image from ${data.aws_ecr_image.ecr.image_pushed_at}"
  source_code_hash = data.aws_ecr_image.ecr.image_digest

  timeout     = "899"
  memory_size = "3000"

  environment {
    variables = {
      KTJS_BUCKET = var.s3_bucket_name
    }
  }

  tags = {
    stack   = var.stack
    prefix  = var.prefix
    flavour = var.flavour
  }
}

module "cloudwatch" {
  source = "../util-lambda-cloudwatch"

  lambda_function_name = aws_lambda_function.f.function_name
  lambda_iam_role_id   = aws_iam_role.iam.id
}
