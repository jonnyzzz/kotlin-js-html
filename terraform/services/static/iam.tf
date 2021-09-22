resource "aws_iam_user" "user" {
  name = local.user
  tags = local.tags
}

resource "aws_iam_policy" "vault" {
  //this policy name is hardcoded in the Vault on buildserver
  name   = "jetsite-production-index-cdn-jetbrains-com-to-idea-index-cdn-jetbrains-com"
  policy = data.aws_iam_policy_document.vault.json
}

resource "aws_iam_user_policy_attachment" "vault" {
  user       = aws_iam_user.user.name
  policy_arn = aws_iam_policy.vault.arn
}

data "aws_iam_policy_document" "vault" {
  statement {
    actions = [
      "cloudfront:CreateInvalidation"
    ]
    resources = [
      "arn:aws:cloudfront::${data.aws_caller_identity.current.account_id}:distribution/${module.website.cf_distribution_id}",
      "arn:aws:cloudfront::${data.aws_caller_identity.current.account_id}:distribution/${module.secured-website.cf_distribution_id}",
    ]
  }

  statement {
    actions = [
      "s3:DeleteObject",
      "s3:GetObject",
      "s3:ListBucket",
      "s3:PutObject"
    ]
    resources = [
      "arn:aws:s3:::${module.website.bucket_name}",
      "arn:aws:s3:::${module.website.bucket_name}/*",
      "arn:aws:s3:::${module.secured-website.bucket_name}",
      "arn:aws:s3:::${module.secured-website.bucket_name}/*",
    ]
  }
}
