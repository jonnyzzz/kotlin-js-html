
module "lambda-builder" {
  source = "./lambda-builder"

  prefix = local.prefix
  stack = local.stack
  flavour = "v1"

  s3_bucket_name = module.website.bucket_name
}

