# Script de Provisionamento 100% Azure CLI para a Função Serverless de Alertas
# Executar no Azure Cloud Shell ou PowerShell/Bash autenticado (az login)

$RESOURCE_GROUP = "rg-techchallenge-fase4"
$LOCATION = "eastus"
$PREFIX = "tc4grp32"

Write-Host "1. Criando Resource Group no Azure [$RESOURCE_GROUP] em $LOCATION..." -ForegroundColor Cyan
az group create --name $RESOURCE_GROUP --location $LOCATION

Write-Host "2. Implantando infraestrutura Serverless com Bicep..." -ForegroundColor Cyan
az deployment group create `
  --resource-group $RESOURCE_GROUP `
  --template-file ./main.bicep `
  --parameters resourcePrefix=$PREFIX

Write-Host "3. Obtendo Connection String do Azure Service Bus..." -ForegroundColor Cyan
$SB_NAMESPACE = az servicebus namespace list --resource-group $RESOURCE_GROUP --query "[0].name" -o tsv
$SB_CONN = az servicebus namespace authorization-rule keys list `
  --resource-group $RESOURCE_GROUP `
  --namespace-name $SB_NAMESPACE `
  --name RootManageSharedAccessKey `
  --query primaryConnectionString -o tsv

Write-Host "Configuração concluída com sucesso no Azure!" -ForegroundColor Green
Write-Host "Namespace Service Bus: $SB_NAMESPACE"
Write-Host "Fila configurada: feedbacks-queue"

