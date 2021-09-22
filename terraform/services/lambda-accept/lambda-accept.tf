variable "prefix" {}
variable "stack" {}

output "lambda_arn" {
  value = aws_lambda_function.f.arn
}

locals {
  code_zip = abspath("${path.module}/../binaries/lambda-config/lambda-config.zip")
}

data "aws_iam_policy_document" "iam" {
  statement {
    actions = [ "sts:AssumeRole"]
    principals {
      identifiers = [ "lambda.amazonaws.com"]
      type = "Service"
    }
    effect = "Allow"
  }
}

resource "aws_iam_role" "iam" {
  name = "${var.prefix}_iam"
  assume_role_policy = data.aws_iam_policy_document.iam.json
}

resource "aws_lambda_function" "f" {
  filename         = local.code_zip
  source_code_hash = filesha256(local.code_zip)

  function_name    = "${var.prefix}_config_lambda"
  role             = aws_iam_role.iam.arn

  handler          = "com.jetbrains.tbe.services.config.ConfigLambda"
  runtime          = "java11"

  timeout     = "60"
  memory_size = "256"

//  environment {
//    variables = {
//
//    }
//  }
}

module "cloudwatch" {
  source = "../util-lambda-cloudwatch"

  lambda_function_name = aws_lambda_function.f.function_name
  lambda_iam_role_id = aws_iam_role.iam.id
}
