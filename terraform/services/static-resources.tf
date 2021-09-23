
locals {
  static_cdn_domain = var.public_dns
  static_cdn_base_url = "https://${local.static_cdn_domain}"
  bucket_name = "${local.prefix}-working-bucket"
}

resource "aws_s3_bucket" "bucket" {
  bucket = local.bucket_name
  acl    = "private"
}

resource "aws_s3_bucket_object" "shim" {
  bucket = local.bucket_name
  key    = "v1/shim.js"
  content = file(abspath("${path.module}/../../frontend-script/shim.js"))
}


