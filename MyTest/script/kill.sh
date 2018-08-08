#!/bin/bash
mainclass="com.ehl.tvc.query.server.QueryServer"
ps -ef | grep $mainclass | grep -v grep | awk '{print $2}' | xargs kill -9 >/dev/null 2>&1  &