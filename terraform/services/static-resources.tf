
module "website" {
  source = "github.com/jetbrains-infra/terraform-aws-static-website?ref=0.0.10"

  aws_region             = data.aws_region.current.id
  use_s3_origin_identity = true
  register_ipv6          = true
  tags                   = {
    Stack = local.stack
    Prefix = local.prefix
    Hackathon21 = ""
    Name = var.public_dns
  }

  route53_zone_name = var.public_dns_zone_part
  domain_name       = "static.${var.public_dns}"
  website_name      = "kotlin-js-html-hackathon21-${local.stack}"
}

