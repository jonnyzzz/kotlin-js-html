terraform {
  backend "s3" {
    bucket = "jetsites-static-states"
    key    = "index-cdn.jetbrains.com/production.tfstate"
    region = "eu-west-1"
  }
}

variable "aws_region" {
  default = "eu-west-1"
}

provider "aws" {
  region = var.aws_region
  version = ">=2.66"
}

data "aws_caller_identity" "current" {}

locals {
  project_name = "index-cdn-jetbrains-com"
  stack_name   = "production"
  route53_zone = "index-cdn.jetbrains.com"
  route53_domain = local.route53_zone
  route53_secured_domain = "secure.${local.route53_zone}"
  team         = "idea"
  user         = "${local.team}-${local.project_name}"
  issue        = "ADM-40920"
  tags = {
    Name  = local.project_name
    Issue = local.issue
    Team  = local.team
  }

}

output "public_bucket_name" {
  value = module.website.bucket_name
}

output "secured_bucket_name" {
  value = module.secured-website.bucket_name
}

output "public_cf_distribution_id" {
  value = module.website.cf_distribution_id
}

output "secured_cf_distribution_id" {
  value = module.secured-website.cf_distribution_id
}

module "website" {
  source = "github.com/jetbrains-infra/terraform-aws-static-website?ref=0.0.10"

  aws_region = var.aws_region
  use_s3_origin_identity = true
  register_ipv6 = true
  tags = merge(local.tags, {Monitoring = "enabled", Name = local.route53_zone})

  route53_zone_name = local.route53_zone
  domain_name       = local.route53_domain
  website_name      = "${local.stack_name}-${local.project_name}"
}

module "secured-website" {
  source = "github.com/jetbrains-infra/terraform-aws-static-website?ref=0.0.10"

  aws_region = var.aws_region
  use_s3_origin_identity = true
  register_ipv6 = true
  tags = merge(local.tags, {Monitoring = "enabled", Name = local.route53_secured_domain})

  route53_zone_name = local.route53_zone
  domain_name       = local.route53_secured_domain
  website_name      = "${local.stack_name}-${local.project_name}-secured"
  lambda_associations = [
    {
      event_type = module.jwt_checker.cloudfront_event_type,
      lambda_arn = module.jwt_checker.qualified_arn
    }
  ]
}

module "jwt_checker" {
  source = "github.com/jetbrains-infra/terraform-aws-oauth-jwt-edge-lambda?ref=v0.0.3"
  lambda_name = "${local.stack_name}-${local.project_name}-secured"
  jwt_public_key =<<EOF
-----BEGIN PUBLIC KEY-----
MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCVGznrRvaw80nCgbpi+Y9BOPiu
w0xnsTzBNk1hCapmuk57zVWhn8TPgsNVY2DKlZFEu4tfxwwzU/GLHWyUqnfRF9Gn
ZQuH4qb3RI7+43n9u89Tvq9xSLSiU9XqdU4J9RhhK6lHkTNLKZBRQEjOP9yXHY8K
s1knhffC4D2Bz4W90wIDAQAB
-----END PUBLIC KEY-----
EOF
}

