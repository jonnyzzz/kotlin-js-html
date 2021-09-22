variable "prefix" {}
variable "stack" {}
variable "s3_bucket_name" {}

output "lambda_arn" {
  value = aws_lambda_function.f.arn
}

locals {
  lambda_dir = abspath("${path.module}/../../../aws-reception-lambda")
  code_zip = abspath("${local.lambda_dir}/build/lambda-reception.zip")
}

#   this makes the actual build here, but we assume it's done expternally
#
#  resource "null_resource" "compile_lambda" {
#    provisioner "local-exec" {
#      command = "./build.sh"
#      working_dir = local.lambda_dir
#    }
#  }

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

    effect  = "Allow"
  }
}

resource "aws_iam_role" "iam" {
  name = "${var.prefix}_iam"
  assume_role_policy = data.aws_iam_policy_document.iam.json
}

resource "aws_iam_role_policy" "iam" {
  role   = aws_iam_role.iam.id
  name = "${var.prefix}_reception_lambda_policy"
  policy = data.aws_iam_policy_document.permissions.json
}

resource "aws_lambda_function" "f" {
  filename         = local.code_zip
  source_code_hash = filesha256(local.code_zip)

  function_name    = "${var.prefix}_reception_lambda"
  role             = aws_iam_role.iam.arn

  handler          = "lambda-reception"
  runtime          = "go1.x"

  timeout     = "60"
  memory_size = "256"

  environment {
    variables = {
      KTJS_BUCKET = var.s3_bucket_name
    }
  }

  tags = {
    stack   = var.stack
    prefix  = var.prefix
  }
}

module "cloudwatch" {
  source = "../util-lambda-cloudwatch"

  lambda_function_name = aws_lambda_function.f.function_name
  lambda_iam_role_id = aws_iam_role.iam.id
}
