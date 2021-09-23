
module "get-lambda" {
  source = "./lambda-get"

  prefix = local.prefix
  stack = local.stack
  s3_bucket_name = local.bucket_name

  static_cdn_url_base = local.static_cdn_base_url
}

resource "aws_api_gateway_resource" "getBase" {
  rest_api_id   = aws_api_gateway_rest_api.api.id
  parent_id = aws_api_gateway_rest_api.api.root_resource_id
  path_part = "get"
}

resource "aws_api_gateway_resource" "get" {
  rest_api_id   = aws_api_gateway_rest_api.api.id
  parent_id = aws_api_gateway_resource.getBase.id
  path_part = "{suffix+}"

}

module "get-lambda-handler-cors" {
  source = "./api-gateway-cors"

  api      = aws_api_gateway_resource.get.rest_api_id
  resource = aws_api_gateway_resource.get.id

  methods  = ["GET"]
}

module "get-lambda-handler" {
  source = "./util-api-gateway-lambda-handler"

  lambda_arn = module.get-lambda.lambda_arn
  rest_api_id = aws_api_gateway_rest_api.api.id

  rest_resource_id = aws_api_gateway_resource.get.id
  rest_resource_path = aws_api_gateway_resource.get.path

  http_method = "GET"
}
