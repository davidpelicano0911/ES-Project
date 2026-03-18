#!/bin/bash

# Script to automatically configure machine IP in .env and terraform.tfvars

# Detect the machine's IP address
if command -v ip >/dev/null 2>&1; then
    # Linux
    MACHINE_IP=$(ip route get 1 | awk '{print $7; exit}')
else
    # macOS
    MACHINE_IP=$(ipconfig getifaddr en0 2>/dev/null || ipconfig getifaddr en1 2>/dev/null)
fi

# Check if IP was detected
if [ -z "$MACHINE_IP" ]; then
    echo "Error: Could not detect machine IP address"
    exit 1
fi

echo "Detected machine IP: $MACHINE_IP"

# Update .env file
echo "Updating .env file..."
sed -i.bak "s|^KC_HOSTNAME_URL=.*|KC_HOSTNAME_URL=http://$MACHINE_IP:8081|" .env
sed -i.bak "s|^SPRING_SECURITY_ISSUER_URI=.*|SPRING_SECURITY_ISSUER_URI=http://$MACHINE_IP:8081/realms/marketing-realm|" .env
sed -i.bak "s|^VITE_BACKEND_HOST=.*|VITE_BACKEND_HOST=http://$MACHINE_IP:8080|" .env
sed -i.bak "s|^VITE_KEYCLOAK_URL=.*|VITE_KEYCLOAK_URL=http://$MACHINE_IP:8081|" .env
sed -i.bak "s|^MACHINE_URL=.*|MACHINE_URL=$MACHINE_IP|" .env

# Update terraform.tfvars file
echo "Updating terraform.tfvars file..."
sed -i.bak "s|^vite_backend_host = .*|vite_backend_host = \"http://$MACHINE_IP:8080\"|" terraform.tfvars
sed -i.bak "s|^vite_keycloak_url = .*|vite_keycloak_url = \"http://$MACHINE_IP:8081\"|" terraform.tfvars
sed -i.bak "s|^spring_security_issuer_uri = .*|spring_security_issuer_uri = \"http://$MACHINE_IP:8081/realms/marketing-realm\"|" terraform.tfvars
sed -i.bak "s|^machine_url = .*|machine_url = \"$MACHINE_IP\"|" terraform.tfvars

echo "✅ Configuration updated successfully!"
echo "Machine IP: $MACHINE_IP"
echo ""
echo "Updated files:"
echo "  - .env"
echo "  - terraform.tfvars"
echo ""
echo "Backup files created with .bak extension"
