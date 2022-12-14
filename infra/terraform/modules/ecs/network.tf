resource "aws_vpc" "proesc_backend_VPC" {
  cidr_block = "192.168.0.0/16"

  enable_dns_support   = true
  enable_dns_hostnames = false

  tags = merge(local.default_tags, { "Name" = local.vpc_name })
}

resource "aws_subnet" "proesc_backend_pub_subnet" {
  count  = var.quantity_of_availibility_zones
  vpc_id = aws_vpc.proesc_backend_VPC.id

  cidr_block = "192.168.${count.index}.0/24"

  map_public_ip_on_launch = true

  availability_zone = local.availibility_zones[count.index]

  tags = merge(local.default_tags, { "Name" = "${local.vpc_name}-sub-pub-${count.index + 1}" })
}

resource "aws_internet_gateway" "proesc_backend_IGW" {
  vpc_id = aws_vpc.proesc_backend_VPC.id

  tags = merge(local.default_tags, { "Name" = "${local.vpc_name}-IGW" })
}

resource "aws_default_route_table" "proesc_backend_RT" {
  default_route_table_id = aws_vpc.proesc_backend_VPC.default_route_table_id

  tags = merge(local.default_tags, { "Name" = "${local.vpc_name}-RT" })
}

resource "aws_route" "public_route" {
  route_table_id         = aws_default_route_table.proesc_backend_RT.id
  destination_cidr_block = "0.0.0.0/0"
  gateway_id             = aws_internet_gateway.proesc_backend_IGW.id

  timeouts {
    create = "5m"
  }
}

resource "aws_route_table_association" "proesc_backend_RT_association" {
  count = var.quantity_of_availibility_zones

  subnet_id      = local.subnets[count.index].id
  route_table_id = aws_default_route_table.proesc_backend_RT.id
}

// TODO: Make the right rules here
resource "aws_network_acl" "proesc_backend_ACL" {
  vpc_id = aws_vpc.proesc_backend_VPC.id

  subnet_ids = [for subnet in local.subnets[*] : subnet.id]

  ingress {
    from_port  = 0
    to_port    = 0
    rule_no    = 100
    action     = "allow"
    protocol   = "-1"
    cidr_block = "0.0.0.0/0"
  }

  egress {
    from_port  = 0
    to_port    = 0
    rule_no    = 200
    action     = "allow"
    protocol   = "-1"
    cidr_block = "0.0.0.0/0"
  }

  tags = local.default_tags
}

resource "aws_vpc_endpoint" "vpc_endpoints_for_ecr" {
  count = length(local.ecs_vpc_endpoints)

  vpc_id            = aws_vpc.proesc_backend_VPC.id
  service_name      = local.ecs_vpc_endpoints[count.index]
  vpc_endpoint_type = "Interface"

  tags = local.default_tags
}