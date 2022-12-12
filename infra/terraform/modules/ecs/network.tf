resource "aws_security_group" "proesc_backend_SG" {
  name        = local.elb_sg_name
  description = "Allow http connections"
  vpc_id      = var.AWS_VPC_ID

  ingress {
    description      = "HTTP anywhere"
    from_port        = 80
    to_port          = 80
    protocol         = "tcp"
    cidr_blocks      = ["0.0.0.0/0"]
    ipv6_cidr_blocks = ["::/0"]
  }

  ingress {
    description      = "HTTPS anywhere"
    from_port        = 443
    to_port          = 443
    protocol         = "tcp"
    cidr_blocks      = ["0.0.0.0/0"]
    ipv6_cidr_blocks = ["::/0"]
  }

  egress {
    from_port        = 0
    to_port          = 0
    protocol         = "-1"
    cidr_blocks      = ["0.0.0.0/0"]
    ipv6_cidr_blocks = ["::/0"]
  }

  tags = local.default_tags
}

resource "aws_security_group" "proesc_backend_TD_SG" {
  name        = local.target_group_sg_name
  description = "Allow http connection from target group"
  vpc_id      = var.AWS_VPC_ID

  ingress {
    description     = "HTTP from target group"
    from_port       = 80
    to_port         = 80
    protocol        = "tcp"
    security_groups = [aws_security_group.proesc_backend_SG.id]
  }

  egress {
    from_port        = 0
    to_port          = 0
    protocol         = "-1"
    cidr_blocks      = ["0.0.0.0/0"]
    ipv6_cidr_blocks = ["::/0"]
  }

  tags = local.default_tags
}