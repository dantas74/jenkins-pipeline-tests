terraform {
  /*backend "s3" {
    bucket = "infra-state"
    key = "proesc-backend/first-state.tfstate"
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

module "dev-stage" {
  source = "../../modules/ecs"

  environment            = "Development"
  AWS_REGION             = var.AWS_REGION
  ECR_URI                = var.ECR_URI
  ECS_EXECUTION_ROLE_ARN = var.ECS_EXECUTION_ROLE_ARN
  APP_KEY                = var.APP_KEY
  AWS_VPC_ID             = var.AWS_VPC_ID
}