

## AWS Secrets Manager testing

Tested with spring-boot 2.6.8 and 

```xml
<properties>
    <java.version>17</java.version>
    <spring-cloud.version>2021.0.3</spring-cloud.version>
    <spring-cloud-aws.version>2.4.1</spring-cloud-aws.version>
</properties>
```


Secrets created in AWS:

```

# Test with endng / , had validation error saying the property was of unknown file format in early testing.
/secret/kube-ns/my-service/test-secret/
{"key1":"value1","key2":"value2","app.message":"AWS says hello 1 there"}

/secret/kube-ns/my-service/test-secret
{"key1":"value3","key2":"value3","app.message":"AWS says hello there 3, no ending slash in secret name"}

/secret/kube-ns/my-service
{"key1":"value11","key2":"value22","app.message":"AWS says hello 2 there"}

```

Configuration loading order

- secrets.config.import
- aws.secretsmanager.x by convention, attempts minimum of two secrets
  - xyz/<name>
  - xyz/<context>


### I only want secrets.config.import loading


```xml

<dependencies>
    <dependency>
        <groupId>io.awspring.cloud</groupId>
        <artifactId>spring-cloud-starter-aws-secrets-manager-config</artifactId>
    </dependency>
</dependencies>

<dependencyManagement>
    <dependencies>
        <dependency>
            <groupId>io.awspring.cloud</groupId>
            <artifactId>spring-cloud-aws-dependencies</artifactId>
            <version>${spring-cloud-aws.version}</version>
            <type>pom</type>
            <scope>import</scope>
        </dependency>
    </dependencies>
</dependencyManagement>
```

```yaml
# All in application.yaml
aws:
  secretsmanager:
    enabled: true

spring:
  config:
    import: aws-secretsmanager:/secret/kube-ns/${spring.application.name}/test-secret/
    use-legacy-processing: false
```


### Notes spring.config.import configuration loading 


```yaml
spring:
  config:
    # These are full secret names
    # Can be prefixed by 'optional:'
    # Multiple secrets delimited with ';'
    import: aws-secretsmanager:/secret/kube-ns/${spring.application.name}/test-secret/
    # If true , then this config is not loaded at all! Is false by default
    use-legacy-processing: false 

```

### Notes aws.secretsmanager.x by convention/naming pattern configuration loading


```xml
<dependencies>
  <!-- Spring Cloud -->
  <dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-bootstrap</artifactId>
  </dependency>

  <dependency>
    <groupId>io.awspring.cloud</groupId>
    <artifactId>spring-cloud-starter-aws-secrets-manager-config</artifactId>
  </dependency>
</dependencies>

<dependencyManagement>
    <dependencies>
        <dependency>
            <groupId>io.awspring.cloud</groupId>
            <artifactId>spring-cloud-aws-dependencies</artifactId>
            <version>${spring-cloud-aws.version}</version>
            <type>pom</type>
            <scope>import</scope>
        </dependency>
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-dependencies</artifactId>
            <version>${spring-cloud.version}</version>
            <type>pom</type>
            <scope>import</scope>
        </dependency>
    </dependencies>
</dependencyManagement>
```


Must be in `boostrap.yaml`

```yaml
aws:
  secretsmanager:
    # Must be enabled, or spring.config.import is also disabled
    enabled: true
    # Make these secrets optional, otherwise service startup is aborted
    fail-fast: false
    prefix: /secret/kube-ns
    # Default is meant to be `spring.application.name` but is null, recon because this runs in boostrap phase
    name: my-service
    # Default is `application`
    # Kind of like a `global` secret
    default-context: application
```


Example logs showing order when both `spring.config.import` and `aws.secretsmanager.xx` is active:

```
2022-07-03 14:10:52.998  INFO 6648 --- [           main] i.a.c.s.AwsSecretsManagerPropertySources : Loading secrets from AWS Secret Manager secret with name: /secret/kube-ns/my-service/test-secret, optional: false
2022-07-03 14:10:52.998 DEBUG 6648 --- [           main] i.a.c.s.AwsSecretsManagerPropertySource  : Populating property retrieved from AWS Secrets Manager: key1
2022-07-03 14:10:52.998 DEBUG 6648 --- [           main] i.a.c.s.AwsSecretsManagerPropertySource  : Populating property retrieved from AWS Secrets Manager: key2
2022-07-03 14:10:52.998 DEBUG 6648 --- [           main] i.a.c.s.AwsSecretsManagerPropertySource  : Populating property retrieved from AWS Secrets Manager: app.message
2022-07-03 14:10:52.998  INFO 6648 --- [           main] i.a.c.s.AwsSecretsManagerPropertySources : Loading secrets from AWS Secret Manager secret with name: /secret/kube-ns/my-service, optional: true
2022-07-03 14:10:52.998 DEBUG 6648 --- [           main] i.a.c.s.AwsSecretsManagerPropertySource  : Populating property retrieved from AWS Secrets Manager: key1
2022-07-03 14:10:52.998 DEBUG 6648 --- [           main] i.a.c.s.AwsSecretsManagerPropertySource  : Populating property retrieved from AWS Secrets Manager: key2
2022-07-03 14:10:52.998 DEBUG 6648 --- [           main] i.a.c.s.AwsSecretsManagerPropertySource  : Populating property retrieved from AWS Secrets Manager: app.message
2022-07-03 14:10:52.998  INFO 6648 --- [           main] i.a.c.s.AwsSecretsManagerPropertySources : Loading secrets from AWS Secret Manager secret with name: /secret/kube-ns/application, optional: true
2022-07-03 14:10:52.998  WARN 6648 --- [           main] i.a.c.s.AwsSecretsManagerPropertySources : Unable to load AWS secret from /secret/kube-ns/application. Secrets Manager can't find the specified secret. (Service: AWSSecretsManager; Status Code: 400; Error Code: ResourceNotFoundException; Request ID: 3922e903-5737-4556-8a20-8102cc47b3be; Proxy: null)

2022-07-03 14:10:55.065  INFO 6648 --- [           main] m.m.a.config.ApplicationConfiguration    : Loaded secrets - key1:value11, key2:value22, app.message:AWS says hello 2 there, app.other-message: Another message in application.yaml
```