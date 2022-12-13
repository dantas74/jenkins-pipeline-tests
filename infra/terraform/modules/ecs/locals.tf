locals {
  stage_suffix = var.map_environment_suffix[var.environment]

  target_group_name = "proesc-backend-TG-${local.stage_suffix}"
  elb_name          = "proesc-backend-ELB-${local.stage_suffix}"

  cluster_name           = "proesc-backend-CLU-${local.stage_suffix}"
  task_definition_family = "proesc-backend-TD-${local.stage_suffix}"
  service_name           = "proesc-backend-SRV-${local.stage_suffix}"
  environment_ecr_uri = "${var.ECR_URI}:${local.stage_suffix}-latest"

  container_definitions_path = "${path.module}/templates/container-definitions.tftpl"

  elb_sg_name          = "proesc-backend-ELB-SG-${local.stage_suffix}"
  target_group_sg_name = "proesc-backend-TD-SG-${local.stage_suffix}"

  default_tags = {
    "Project"     = "Proesc Backend"
    "Environment" = var.environment
    "Owner"       = "Proesc"
    "Maintainer"  = "Matheus Dantas Ricardo"
    "Maintainer Contact" = "matheus-dr@proton.me"
  }
}