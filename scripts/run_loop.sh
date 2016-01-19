#!/bin/bash

COMMAND="$1"
shift

while true;
do
    "$COMMAND" "$@"
    sleep 30
done
