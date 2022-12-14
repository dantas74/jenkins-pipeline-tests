terraform {
  /*backend "s3" {
    bucket = "infra-state"
    key = "proesc-backend/prod-state.tfstate"
    region = "sa-east-1"
  }*/

  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = "~> 4.0"
    }
  }
}

provider "aws" {
  region = var.AWS_REGION
  //access_key = var.ACCESS_KEY
  //secret_key = var.SECRET_KEY
}

module "prod-stage" {
  source = "../../modules/ecs"

  environment                    = "Production"
  quantity_of_availibility_zones = 3
  AWS_REGION                     = var.AWS_REGION
  ECR_URI                        = var.ECR_URI
  ECS_EXECUTION_ROLE_ARN         = var.ECS_EXECUTION_ROLE_ARN
  APP_KEY                        = var.APP_KEY
}