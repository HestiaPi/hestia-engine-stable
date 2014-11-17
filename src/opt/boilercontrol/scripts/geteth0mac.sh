#!/bin/bash

ip addr show eth0 | grep link | awk '{print $2}' | cut -d/ -f1
