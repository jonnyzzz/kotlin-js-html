variable "prefix" {}
variable "stack" {}
variable "s3_bucket_name" {}
variable "flavour" {}
variable "static_cdn_url_base" {}

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

