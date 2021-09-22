
module "reception-lambda" {
  source = "./lambda-reception"

  prefix = local.prefix
  stack = local.stack
}

resource "aws_api_gateway_resource" "reception" {
  rest_api_id   = aws_api_gateway_rest_api.api.id
  parent_id = aws_api_gateway_rest_api.api.root_resource_id
  path_part = "reception"
}

module "reception-lambda-handler-cors" {
  source = "./api-gateway-cors"

  api      = aws_api_gateway_resource.reception.rest_api_id
  resource = aws_api_gateway_resource.reception.id

  methods  = ["GET", "POST"]
}

module "reception-lambda-handler" {
  source = "./util-api-gateway-lambda-handler"

  lambda_arn = module.reception-lambda.lambda_arn
  rest_api_id = aws_api_gateway_rest_api.api.id

  rest_resource_id = aws_api_gateway_resource.reception.id
  rest_resource_path = aws_api_gateway_resource.reception.path
}
