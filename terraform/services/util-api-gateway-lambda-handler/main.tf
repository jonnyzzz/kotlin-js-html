variable "rest_api_id" {}

variable "rest_resource_id" {}
variable "rest_resource_path" {
  //TODO: the problem here is that we need to know URL path on
  //TODO: what the given ${var.rest_resource_id} is pointing.
  //TODO: currently, this information is not known here
  //TODO: also, there are different APIs to for the root resource and a child resource
}

variable lambda_arn {}

data aws_caller_identity current {}
data aws_region current { }

resource "aws_api_gateway_method" "method" {
  rest_api_id = var.rest_api_id
  resource_id = var.rest_resource_id
  http_method = "POST"
  authorization = "NONE"

  request_parameters = {
    "method.request.header.Authorization" = true
  }
}

resource "aws_api_gateway_integration" "method" {
  rest_api_id = aws_api_gateway_method.method.rest_api_id
  resource_id = aws_api_gateway_method.method.resource_id
  http_method = aws_api_gateway_method.method.http_method
  type        = "AWS_PROXY"
  uri         = "arn:aws:apigateway:${data.aws_region.current.name}:lambda:path/2015-03-31/functions/${var.lambda_arn}/invocations"

  integration_http_method = "POST"
}

resource "aws_lambda_permission" "method" {
  statement_id  = "AllowExecutionFromAPIGateway"
  action        = "lambda:InvokeFunction"
  function_name = var.lambda_arn
  principal     = "apigateway.amazonaws.com"

  # More: http://docs.aws.amazon.com/apigateway/latest/developerguide/api-gateway-control-access-using-iam-policies-to-invoke-api.html
  source_arn = "arn:aws:execute-api:${data.aws_region.current.name}:${data.aws_caller_identity.current.account_id}:${var.rest_api_id}/*/${aws_api_gateway_method.method.http_method}/${trimprefix(var.rest_resource_path, "/")}"
}

output "rest_api_id" {
  value = var.rest_api_id
}

output "method_path" {
  value = "${var.rest_resource_path == "" ? "" : "${var.rest_resource_path}/"}${aws_api_gateway_method.method.http_method}"
}
