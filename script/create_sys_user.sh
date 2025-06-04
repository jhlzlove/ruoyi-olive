#!/bin/bash

# 创建用户，并关联到 elasticsearch 组，禁止登录 shell
# -M 不创建用户主目录（根据需求可选是否创建）
# -s 禁止登录（安全推荐）
# -g 指定所属组
sudo groupadd elasticsearch
sudo useradd \
  -M \
  -s /sbin/nologin \
  -g elasticsearch \
  elasticsearch
