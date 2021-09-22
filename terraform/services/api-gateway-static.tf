resource "aws_api_gateway_method" "root_get" {
  rest_api_id   = aws_api_gateway_rest_api.api.id
  resource_id   = aws_api_gateway_rest_api.api.root_resource_id
  http_method   = "GET"
  authorization = "NONE"
}

resource "aws_api_gateway_integration" "root_get" {
  rest_api_id = aws_api_gateway_method.root_get.rest_api_id
  resource_id = aws_api_gateway_method.root_get.resource_id
  http_method = aws_api_gateway_method.root_get.http_method
  type        = "MOCK"

  passthrough_behavior = "NEVER"
  request_templates = {
    "text/plain"       = "{\"statusCode\": 200}"
    "application/json" = "{\"statusCode\": 200}"
  }
}

resource "aws_api_gateway_method_response" "root_get200" {
  rest_api_id = aws_api_gateway_method.root_get.rest_api_id
  resource_id = aws_api_gateway_method.root_get.resource_id
  http_method = aws_api_gateway_method.root_get.http_method
  status_code = "200"

  response_models = {
    "text/plain" = "Empty"
  }
}

resource "aws_api_gateway_integration_response" "root_get" {
  rest_api_id = aws_api_gateway_method_response.root_get200.rest_api_id
  resource_id = aws_api_gateway_method_response.root_get200.resource_id
  http_method = aws_api_gateway_method_response.root_get200.http_method
  status_code = aws_api_gateway_method_response.root_get200.status_code

  response_templates = {
    "text/plain" = "This is kt-html JS static page"
  }
}
