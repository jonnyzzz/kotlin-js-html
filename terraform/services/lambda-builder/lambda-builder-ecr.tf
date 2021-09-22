
data "aws_iam_policy_document" "ecr" {
  statement {
    effect  = "Allow"
    actions = [
      "ecr:BatchGetImage",
      "ecr:GetDownloadUrlForLayer"
    ]
    principals {
      identifiers = ["lambda.amazonaws.com"]
      type        = "Service"
    }
  }
}

data "aws_ecr_image" "ecr" {
  repository_name = aws_ecr_repository.ecr.name
  image_tag = local.image_tag
}

resource "aws_ecr_repository_policy" "ecr" {
  repository = aws_ecr_repository.ecr.id
  policy     = data.aws_iam_policy_document.ecr.json
}

resource "aws_ecr_repository" "ecr" {
  name = "${var.stack}-builder-${var.flavour}"
  image_scanning_configuration {
    scan_on_push = true
  }

  image_tag_mutability = "MUTABLE"
  tags = {
    stack   = var.stack
    prefix  = var.prefix
    flavour = var.flavour
  }
}

data "aws_region" "aws" {

}

resource "local_file" "ecr" {
  filename = local.include_file
  content = <<EOT

AWS_REGION=${data.aws_region.aws.name}
ECR_URL=${aws_ecr_repository.ecr.repository_url}
ECR_TAG=${local.image_tag}

EOT
}

