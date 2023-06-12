# akto

## Introduction

Run Akto tests in Jenkins CI/CD

## Getting started

### Installing the plugin

1. Navigate to Manage Jenkins > Manage Plugins > Advanced Settings.
2. Scroll down to the Deploy Plugin section and click **Choose File**.
3. Upload the akto.hpi file and wait for the plugin to install.

### Freestyle Project

The akto plugin can be used in a freestyle project by selecting the akto plugin in the Post-build Actions dropdown box and then filling in the required inputs

### Pipeline

The akto plugin supports pipelines and can be used in the following ways:

Declarative syntax:

```groovy
node {
	akto aktoApiKey: 'AKTO_API_KEY', aktoDashboardUrl: 'AKTO_DASHBOARD_URL', aktoTestId: 'AKTO_TEST_ID', aktoStartTestDelay: 'AKTO_START_TEST_DELAY'
}
```

Scripted syntax:

```groovy
pipeline {  
  agent any  
  stages {  
    stage('Build') {  
      steps {  
        // build steps go here  
      }  
    }  
  }  
  post {  
      akto aktoApiKey: 'AKTO_API_KEY', aktoDashboardUrl: 'AKTO_DASHBOARD_URL', aktoTestId: 'AKTO_TEST_ID', aktoStartTestDelay: 'AKTO_START_TEST_DELAY'
  }  
}
```

