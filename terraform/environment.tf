terraform {
  required_version = "~>1.0.2"
  required_providers {
    aws = {
      version = "~>3.49.0"
    }
  }

  backend "s3" {
    bucket = "kt-html-js-hackathon21.sandbox.tfstate"
    key = "terraform.tfstate"
    profile = "jetbrains-sandbox"
    region = "eu-central-1"
  }
}

provider "aws" {
  region = "eu-central-1"
  profile = "jetbrains-sandbox"
}

provider "aws" {
  alias = "api_gateway"
  region = "us-east-1"
  profile = "jetbrains-sandbox"
}

locals {
  stack = "kt-js-hackathon21"
}
#
#
#module "tbe-services" {
#  source = "services"
#  stack = local.stack
#
#  public_dns = "tbe.sandbox.intellij.net"
#  public_dns_zone_part = "sandbox.intellij.net."
#
#  providers = {
#    aws = aws
#    aws.api_gateway = aws.api_gateway
#  }
#}
