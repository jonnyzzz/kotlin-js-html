variable "lambda_iam_role_id" {}
variable "lambda_function_name" {}

locals {
  target_logs = "/aws/lambda/${var.lambda_function_name}"
}

data aws_caller_identity current { }
data aws_region current  { }

resource aws_cloudwatch_log_group logs {
  name = local.target_logs
  retention_in_days = 60
}

data aws_iam_policy_document logs {
  statement {
    actions = ["logs:CreateLogStream", "logs:PutLogEvents"]
    resources = ["arn:aws:logs:${data.aws_region.current.name}:${data.aws_caller_identity.current.account_id}:log-group:${local.target_logs}:*" ]
    effect = "Allow"
  }
}

resource aws_iam_role_policy logs {
  depends_on = [aws_cloudwatch_log_group.logs]
  role = var.lambda_iam_role_id
  policy = data.aws_iam_policy_document.logs.json
}
