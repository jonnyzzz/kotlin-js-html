
variable "stack" {
  description = "We use the stack to mark all created resources"
}

variable "public_dns_zone_part" {
  description = "Route53 zone, where we create our record to register with API Gateway"
}

variable "public_dns" {
  description = "Full URL of the service"
}


locals {
  prefix = var.stack
  stack = var.stack
}

terraform {
  required_providers {
    aws = {
      source = "hashicorp/aws"
      version = ">3.39.0"
      configuration_aliases = [ aws.api_gateway ]
    }
  }
}
