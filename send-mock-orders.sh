#!/bin/bash

MOCK_DATA_FILE="data/mock-order-requests.txt"
CREATE_ORDER_URL="http://localhost:8081/orders"
CONTENT_TYPE_HEADER="Content-Type: application/json"

DELAY=0
ITERATIONS=1000
SILENT_MODE=false

if [ $# -eq 0 ]; then
  echo "Usage: $0 [-d delay] [-i iterations] [-s]"
  echo
  echo "Options:"
  echo "  -d DELAY      Set delay between requests in seconds (default: 0)"
  echo "  -i ITERATIONS Set the number of iterations (requests to send) (default: 1000)"
  echo "  -s            Enable silent mode (no output)"
  echo
  echo "By default, the script will use the following options:"
  echo "  Delay: $DELAY sec"
  echo "  Iteration: $ITERATIONS"
  echo "  Silent mode: $SILENT_MODE"
  echo
fi

while getopts ":d:i:s" opt; do
  case $opt in
  s) # Silent mode
    SILENT_MODE=true
    ;;
  d) # Delay (in seconds)
    DELAY=$OPTARG
    ;;
  i) # Iterations
    if [[ $OPTARG -ge 1 && $OPTARG -le 1000 ]]; then
      ITERATIONS=$OPTARG
    else
      echo "Iteration option (-i) must be between 1 and 1000"
      exit 1
    fi
    ;;
  *) # Invalid parameters
    echo "Usage: $0 [-d delay] [-i iterations] [-s]"
    exit 1
  esac
done

if [ $# -ne 0 ]; then
  echo "Script is starting with following options:"
  echo
  echo "Options:"
  echo "  Delay: $DELAY sec"
  echo "  Iterations: $ITERATIONS"
  echo "  Silent mode: $SILENT_MODE"
  echo
fi

sleep 2

while IFS= read -r line; do

  http_response_code=$(curl -s -o /dev/null -w "%{http_code}" -X POST "$CREATE_ORDER_URL" -H "$CONTENT_TYPE_HEADER" -d "$line")
  if ! $SILENT_MODE; then
    if [ "$http_response_code" == "201" ]; then
      echo "Request send $line"
    else
      echo "Request failed with status code $http_response_code"
    fi
  fi

  if [ "$DELAY" -gt 0 ]; then
    sleep "$DELAY"
  fi

done < <(head -n "$ITERATIONS" $MOCK_DATA_FILE)