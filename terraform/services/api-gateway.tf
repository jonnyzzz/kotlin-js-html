
resource "aws_api_gateway_deployment" "ktjs" {
  depends_on = [
    aws_api_gateway_integration.root_get,
    aws_api_gateway_method.root_get,

#    module.config-feed-lambda-handler,
#    module.config-feed-lambda,

  ]

//  variables {
    //A Hack to include dependency on code change
//    "lambda_code_hash" = "${sha256(var.code_zip_hash)}"
//  }
  rest_api_id = aws_api_gateway_rest_api.api.id

  //https://github.com/hashicorp/terraform/issues/10674
  stage_name = replace("${local.prefix}_stage", "-", "_")

  lifecycle {
    create_before_destroy = true
  }
}

resource "aws_api_gateway_domain_name" "api" {
  domain_name = var.public_dns
  certificate_arn = aws_acm_certificate.api.arn
  depends_on = [aws_acm_certificate_validation.api]
}

resource "aws_acm_certificate" "api" {
  domain_name               = var.public_dns
  subject_alternative_names = []
  validation_method         = "DNS"

  tags = {
    Name    = "${local.prefix} for kt-html-js hackathon 21"
    stack = local.stack
  }

  provider = aws.api_gateway
}

resource "aws_route53_record" "proof" {
  for_each = {
    for dvo in aws_acm_certificate.api.domain_validation_options : dvo.domain_name => {
      name   = dvo.resource_record_name
      record = dvo.resource_record_value
      type   = dvo.resource_record_type
    }
  }

  allow_overwrite = true
  name            = each.value.name
  records         = [each.value.record]
  ttl             = 60
  type            = each.value.type
  zone_id         = data.aws_route53_zone.api.zone_id
}

resource "aws_acm_certificate_validation" "api" {
  certificate_arn         = aws_acm_certificate.api.arn
  validation_record_fqdns =  [for record in aws_route53_record.proof : record.fqdn]
  provider = aws.api_gateway
}

resource "aws_route53_record" "service" {
  zone_id = data.aws_route53_zone.api.zone_id

  name = aws_api_gateway_domain_name.api.domain_name
  type = "A"

  alias {
    name                   = aws_api_gateway_domain_name.api.cloudfront_domain_name
    zone_id                = aws_api_gateway_domain_name.api.cloudfront_zone_id
    evaluate_target_health = false
  }
}

resource "aws_api_gateway_base_path_mapping" "test" {
  api_id      = aws_api_gateway_rest_api.api.id
  stage_name  = aws_api_gateway_deployment.ktjs.stage_name
  domain_name = aws_api_gateway_domain_name.api.domain_name
}

resource "aws_api_gateway_rest_api" "api" {
  name = local.prefix
}

data "aws_caller_identity" "current" {}
data "aws_region" "current" { }

data "aws_route53_zone" "api" {
  name         = var.public_dns_zone_part
  private_zone = false
}
