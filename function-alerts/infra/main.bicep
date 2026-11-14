@description('Localização geográfica dos recursos Azure')
param location string = resourceGroup().location

@description('Prefixo único para nomes dos recursos')
param resourcePrefix string = 'tc4grp32'

var uniqueSuffix = substring(uniqueString(resourceGroup().id), 0, 6)

// 1. Storage Account para a Azure Function (obrigatório para runtime serverless)
var storageAccountName = toLower('${resourcePrefix}st${uniqueSuffix}')
resource storageAccount 'Microsoft.Storage/storageAccounts@2023-05-01' = {
  name: storageAccountName
  location: location
  sku: {
    name: 'Standard_LRS'
  }
  kind: 'StorageV2'
  properties: {
    minimumTlsVersion: 'TLS1_2'
    supportsHttpsTrafficOnly: true
  }
}

// 2. Application Insights & Log Analytics (Observabilidade Serverless)
resource logAnalyticsWorkspace 'Microsoft.OperationalInsights/workspaces@2023-09-01' = {
  name: '${resourcePrefix}-law-${uniqueSuffix}'
  location: location
  properties: {
    sku: {
      name: 'PerGB2018'
    }
    retentionInDays: 30
  }
}

resource appInsights 'Microsoft.Insights/components@2020-02-02' = {
  name: '${resourcePrefix}-insights-${uniqueSuffix}'
  location: location
  kind: 'web'
  properties: {
    Application_Type: 'web'
    WorkspaceResourceId: logAnalyticsWorkspace.id
  }
}

// 3. Azure Service Bus (Mensageria Enterprise / Gatilho da Função)
var serviceBusNamespaceName = '${resourcePrefix}-sb-${uniqueSuffix}'
resource serviceBusNamespace 'Microsoft.ServiceBus/namespaces@2022-10-01-preview' = {
  name: serviceBusNamespaceName
  location: location
  sku: {
    name: 'Standard'
    tier: 'Standard'
  }
}

resource feedbackQueue 'Microsoft.ServiceBus/namespaces/queues@2022-10-01-preview' = {
  parent: serviceBusNamespace
  name: 'feedbacks-queue'
  properties: {
    lockDuration: 'PT1M'
    maxDeliveryCount: 5
    deadLetteringOnMessageExpiration: true
  }
}

// Chave de autorização com permissões Listen/Send
resource serviceBusAuthRule 'Microsoft.ServiceBus/namespaces/AuthorizationRules@2022-10-01-preview' = {
  parent: serviceBusNamespace
  name: 'RootManageSharedAccessKey'
  properties: {
    rights: [
      'Listen'
      'Send'
      'Manage'
    ]
  }
}

// 4. Hosting Plan Serverless (Consumption Y1 Linux)
resource hostingPlan 'Microsoft.Web/serverfarms@2023-12-01' = {
  name: '${resourcePrefix}-plan-${uniqueSuffix}'
  location: location
  sku: {
    name: 'Y1'
    tier: 'Dynamic'
  }
  kind: 'functionapp,linux'
  properties: {
    reserved: true
  }
}

// 5. Azure Function App (Linux, Java 21)
var functionAppName = '${resourcePrefix}-func-alerts-${uniqueSuffix}'
resource functionApp 'Microsoft.Web/sites@2023-12-01' = {
  name: functionAppName
  location: location
  kind: 'functionapp,linux'
  identity: {
    type: 'SystemAssigned'
  }
  properties: {
    serverFarmId: hostingPlan.id
    reserved: true
    siteConfig: {
      linuxFxVersion: 'JAVA|21'
      appSettings: [
        {
          name: 'AzureWebJobsStorage'
          value: 'DefaultEndpointsProtocol=https;AccountName=${storageAccount.name};EndpointSuffix=${environment().suffixes.storage};AccountKey=${storageAccount.listKeys().keys[0].value}'
        }
        {
          name: 'FUNCTIONS_EXTENSION_VERSION'
          value: '~4'
        }
        {
          name: 'FUNCTIONS_WORKER_RUNTIME'
          value: 'java'
        }
        {
          name: 'APPINSIGHTS_INSTRUMENTATIONKEY'
          value: appInsights.properties.InstrumentationKey
        }
        {
          name: 'APPLICATIONINSIGHTS_CONNECTION_STRING'
          value: appInsights.properties.ConnectionString
        }
        {
          name: 'ServiceBusConnectionString'
          value: listKeys(serviceBusAuthRule.id, serviceBusAuthRule.apiVersion).primaryConnectionString
        }
        {
          name: 'FEEDBACK_QUEUE_NAME'
          value: 'feedbacks-queue'
        }
        {
          name: 'ALERT_RECIPIENT_EMAIL'
          value: 'coordenacao-feedbacks@instituicao.edu.br'
        }
        {
          name: 'ALERT_SENDER_EMAIL'
          value: 'DoNotReply@feedbacks.azurecomm.net'
        }
        {
          name: 'COMMUNICATION_SERVICES_CONNECTION_STRING'
          value: ''
        }
      ]
    }
  }
}

output functionAppName string = functionApp.name
output serviceBusQueueName string = feedbackQueue.name
output serviceBusNamespace string = serviceBusNamespace.name

