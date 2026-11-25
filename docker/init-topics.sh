#!/bin/bash

echo "Menunggu Kafka broker (kafka-1) siap..."
# Loop sederhana mengecek ketersediaan port

until /opt/bitnami/kafka/bin/kafka-topics.sh --bootstrap-server kafka-1:9090 --list > /dev/null 2>&1; do
  echo "Port belum siap, mencoba ulang dalam 2 detik.., sabar yak!"
  sleep 2
done

echo "Memulai pembuatan multiple topics..."

TOPICS=(
  "order-confirm"
  "order-cancel"
  "order-payment-fail"
  "order-created"
  "order-confirmed"
  "order-cancelled"
  "inventory-reserve"
  "inventory-release"
  "inventory-confirm"
  "inventory-reserved"
  "inventory-failed"
  "inventory-released"
  "payment-charge"
  "payment-refund"
  "payment-charged"
  "payment-failed"
  "order-dlq-events"

)

for topic in "${TOPICS[@]}"; do
  echo "-> Membuat topic: $topic"
  /opt/bitnami/kafka/bin/kafka-topics.sh --create --if-not-exists \
    --bootstrap-server kafka-1:9090 \
    --topic "$topic" \
    --partitions 3 \
    --replication-factor 3 \
    --config min.insync.replicas=2
done

echo "Semua topic berhasil dibuat!"