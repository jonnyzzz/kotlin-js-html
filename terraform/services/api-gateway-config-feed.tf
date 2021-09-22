#
#module "config-feed-lambda" {
#  source = "lambda-accept"
#
#  prefix = local.prefix
#  stack = local.stack
#}
#
#resource "aws_api_gateway_resource" "config-feed" {
#  rest_api_id   = aws_api_gateway_rest_api.api.id
#  parent_id = aws_api_gateway_rest_api.api.root_resource_id
#  path_part = "config"
#}
#
#resource "aws_api_gateway_resource" "config-feed-v1" {
#  rest_api_id   = aws_api_gateway_rest_api.api.id
#  parent_id = aws_api_gateway_resource.config-feed.id
#  path_part = "v1"
#}
#
#module "config-feed-lambda-handler" {
#  source = "./util-api-gateway-lambda-handler"
#
#  lambda_arn = module.config-feed-lambda.lambda_arn
#  rest_api_id = aws_api_gateway_rest_api.api.id
#
#  rest_resource_id = aws_api_gateway_resource.config-feed-v1.id
#  rest_resource_path = aws_api_gateway_resource.config-feed-v1.path
#}
