#!/bin/bash

CONNECTOR_NAME="order-outbox-connector"
DEBEZIUM_CONNECT_URL="http://localhost:8083"
CONNECTOR_CONFIG_FILE="data/$CONNECTOR_NAME.json"

HTTP_CODE=$(curl -s -o /dev/null -w "%{http_code}" "$DEBEZIUM_CONNECT_URL/connectors/$CONNECTOR_NAME")

if [ "$HTTP_CODE" == "200" ]; then
  echo "Connector already exists"
else

  if [ ! -f "$CONNECTOR_CONFIG_FILE" ]; then
    echo "Config file not found: $CONNECTOR_CONFIG_FILE"
    exit 1
  fi

  curl -s -X POST "$DEBEZIUM_CONNECT_URL/connectors" \
    -H "Content-Type: application/json" \
    -d "@$CONNECTOR_CONFIG_FILE"

fi