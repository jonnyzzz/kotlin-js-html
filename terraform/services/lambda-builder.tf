
module "lambda-builder" {
  source = "./lambda-builder"

  prefix = local.prefix
  stack = local.stack
  flavour = "v1"

  s3_bucket_name = module.website.bucket_name
  static_cdn_url_base = local.static_cdn_base_url
}

