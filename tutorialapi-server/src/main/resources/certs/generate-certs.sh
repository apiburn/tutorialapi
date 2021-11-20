#!/bin/sh

cd "$(dirname $0)"

# Create CA certificate and key

openssl genrsa -out ca.priv 2048

openssl req -x509 -new -nodes -sha256 -days 1825 -key ca.priv \
        -out ca.crt -subj "/C=US/O=tutorialapi/CN=ca"

# Create server certificate and key

openssl genrsa -out tutorialapi.priv 2048

openssl req -new -key tutorialapi.priv -out tutorialapi.csr \
        -subj "/C=US/O=tutorialapi/CN=tutorialapi.com"

cat <<EOF >tutorialapi.ext
subjectAltName = @alt_names
[alt_names]
DNS.1 = localhost
DNS.2 = tutorialapi.com
DNS.3 = www.tutorialapi.com
EOF

openssl x509 -req -in tutorialapi.csr -CA ca.crt -CAkey ca.priv \
        -out tutorialapi.crt -CAcreateserial  -days 1825 -sha256 \
        -extfile tutorialapi.ext

openssl pkcs12 -export -in tutorialapi.crt -inkey tutorialapi.priv \
        -out tutorialapi.p12 -certfile tutorialapi.crt \
        -password pass:changeit -name tutorialapi

openssl pkcs12 -in tutorialapi.p12 -out tutorialapi.pub \
        -clcerts -nokeys -passin pass:changeit
openssl pkcs12 -in tutorialapi.p12 -out tutorialapi.pem \
        -nodes -passin pass:changeit
