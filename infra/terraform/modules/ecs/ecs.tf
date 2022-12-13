resource "aws_ecs_cluster" "proesc_backend_CLU" {
  name = local.cluster_name

  setting {
    name  = "containerInsights"
    value = "enabled"
  }

  tags = local.default_tags
}

resource "aws_ecs_task_definition" "proesc_backend_TD" {
  family             = local.task_definition_family
  execution_role_arn = var.ECS_EXECUTION_ROLE_ARN
  task_role_arn      = var.ECS_EXECUTION_ROLE_ARN

  cpu    = 1024
  memory = 2048

  requires_compatibilities = ["FARGATE"]
  network_mode             = "awsvpc"

  container_definitions = templatefile(local.container_definitions_path, {
    ECR_URI = local.environment_ecr_uri
    APP_KEY = var.APP_KEY
  })

  runtime_platform {
    operating_system_family = "LINUX"
    cpu_architecture        = "X86_64"
  }

  volume {
    name = "app-VOL"
  }

  tags = local.default_tags
}

resource "aws_ecs_service" "proesc-backend-SRV" {
  name                   = local.service_name
  cluster                = aws_ecs_cluster.proesc_backend_CLU.id
  task_definition        = aws_ecs_task_definition.proesc_backend_TD.arn
  desired_count          = 1
  launch_type            = "FARGATE"
  enable_execute_command = true
  force_new_deployment   = true
  scheduling_strategy    = "REPLICA"

  deployment_minimum_healthy_percent = 50
  deployment_maximum_percent         = 100

  load_balancer {
    target_group_arn = aws_lb_target_group.proesc_backend_TG.arn
    container_name   = "app"
    container_port   = 80
  }

  network_configuration {
    security_groups = [aws_security_group.proesc_backend_SG.id]

    subnets = var.default_subnets

    assign_public_ip = true
  }

  tags = local.default_tags
}