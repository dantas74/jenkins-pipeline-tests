resource "aws_lb_target_group" "proesc_backend_TG" {
  name        = local.target_group_name
  port        = 80
  protocol    = "HTTP"
  target_type = "ip"
  vpc_id      = aws_vpc.proesc_backend_VPC.id

  health_check {
    enabled             = true
    matcher             = "200"
    path                = "/"
    protocol            = "HTTP"
    healthy_threshold   = 5
    unhealthy_threshold = 5
  }
}

resource "aws_lb" "proesc_backend_ELB" {
  name               = local.elb_name
  internal           = false
  load_balancer_type = "application"
  security_groups    = [aws_security_group.proesc_backend_SG.id]
  subnets            = [for subnet in local.subnets[*] : subnet.id]

  tags = local.default_tags
}

resource "aws_lb_listener" "proesc_backend_forward_rule" {
  load_balancer_arn = aws_lb.proesc_backend_ELB.arn
  port              = "80"
  protocol          = "HTTP"

  default_action {
    type             = "forward"
    target_group_arn = aws_lb_target_group.proesc_backend_TG.arn
  }
}