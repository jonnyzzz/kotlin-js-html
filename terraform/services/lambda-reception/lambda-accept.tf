variable "prefix" {}
variable "stack" {}

output "lambda_arn" {
  value = aws_lambda_function.f.arn
}

locals {
  lambda_dir = abspath("${path.module}/../../aws-reception-lambda")
  code_zip = abspath("${local.lambda_dir}/biuld/lambda-reception.zip")
}

resource "null_resource" "compile_lambda" {
  provisioner "local-exec" {
    command = "build.sh"
    working_dir = local.lambda_dir
  }
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
  runtime          = "go1.x"

  timeout     = "60"
  memory_size = "256"

  depends_on = [null_resource.compile_lambda]

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
