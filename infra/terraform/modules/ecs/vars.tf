variable "environment" {}

variable "map_environment_suffix" {
  type = map(string)

  default = {
    "Development" = "dev"
    "Production"  = "prod"
    "Release"     = "qa"
  }
}

variable "quantity_of_availibility_zones" {
  default = 3
}

variable "AWS_REGION" {
  default = "sa-east-1"
}

variable "ECR_URI" {}

variable "ECS_EXECUTION_ROLE_ARN" {}

variable "APP_KEY" {}