#!/bin/bash

df | grep rootfs  | awk '{print $5}'
