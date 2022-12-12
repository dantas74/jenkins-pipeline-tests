variable "AWS_REGION" {
  default = "sa-east-1"
}

variable "ECR_URI" {}

variable "ECS_EXECUTION_ROLE_ARN" {}

variable "APP_KEY" {}

variable "AWS_VPC_ID" {}

variable "environment" {}

variable "map_environment_suffix" {
  type = map(string)

  default = {
    "Development" = "dev"
    "Production"  = "prod"
    "Release"     = "qa"
  }
}

variable "default_subnets" {
  type = list(string)

  default = [
    "subnet-0eed9f09c83d3fa07",
    "subnet-0d844ac9414eb17eb",
    "subnet-076f617d1d8b9e8f3"
  ]
}