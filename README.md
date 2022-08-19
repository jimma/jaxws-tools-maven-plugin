# jaxws-tools-maven-plugin

Maven plugin jaxws-tools-maven-plugin provides supports for wsconsume and wsprovide.

**Note**, prior to version 1.2.0.Final of this plugin the plugin name was maven-jaxws-tools-plugin.

**wsconsume** "consumes" the abstract contract (WSDL file) and produces portable JAX-WS service
and client artifacts. 

**wsprovide** generates portable JAX-WS artifacts for a service endpoint implementation.  It
can "provide" the abstract contract (WSDL file) for your service as well.



## POM file configuration
The plugin itself does not have an explicit dependency to a JBossWS stack.  It is meant for
use with implementations of any supported version of the JBossWS SPI.  The user is expected
to set a dependency in his own pom.xml to the desired JBossWS stack version. The plugin
will rely on that for the proper tooling.  The user must add a reference to
org.jboss.ws.cxf:jbossws-cxf-client in his maven dependencies.


```xml
<dependencies>
  <dependency>
    <groupId>org.jboss.ws.cxf</groupId>
    <artifactId>jbossws-cxf-client</artifactId>
    <version>5.5.0.Final</version>
    <scope>provided</scope>
  </dependency>
</dependencies>
```

**Note**
jaxws-tools-maven-plugin 1.3.0.Final only works with jbossws-cxf-client 5.5.0 Final and above javax version
after log4j dependency is removed from jbossws-cxf.


## wsconsume configuration

### wsconsume maven Goals
| goal | comment |
|------|---------|
|wsconsume | triggers the sources generation during __generate-sources__ phase |
|wsconsume-test | triggers the sources generation during __generate-test-sources__ phase |


### wsconsume maven plugin example

In this example the plugin is configured to run in the generate-sources phase.
This plugin consumes the test.wsdl file and generates SEI and wrappers' java sources.
The generated sources are then compiled together with the other project classes.


```xml
<build>
  <plugins>
    <plugin>
      <groupId>org.jboss.ws.plugins</groupId>
      <artifactId>jaxws-tools-maven-plugin</artifactId>
      <version>1.3.0.Final</version>
      <configuration>
        <wsdls>
          <wsdl>${basedir}/test.wsdl</wsdl>
        </wsdls>
      </configuration>
      <executions>
        <execution>
          <goals>
            <goal>wsconsume</goal>
          </goals>
        </execution>
      </executions>
    </plugin>
  </plugins>
</build>
```


In this example multiple wsdl files are declared for processing.  In addition
it directs the target packaging to use SOAP 1.2 binding.  Lastly the plugin is
instructed to be verbose in its processing information.

```xml
<build>
  <plugins>
    <plugin>
      <groupId>org.jboss.ws.plugins</groupId>
      <artifactId>jaxws-tools-maven-plugin</artifactId>
      <version>1.3.0.Final</version>
      <configuration>
       <wsdls>
        <wsdl>${basedir}/test.wsdl</wsdl>
        <wsdl>${basedir}/test2.wsdl</wsdl>
       </wsdls>
       <targetPackage>foo.bar</targetPackage>
       <extension>true</extension>
       <verbose>true</verbose>
      </configuration>
      <executions>
        <execution>
          <goals>
            <goal>wsconsume</goal>
          </goals>
        </execution>
      </executions>
    </plugin>
  </plugins>
</build>
```

In both examples above changing the goal from wsconsume to wsconsume-test
will result in the wsdl being processed for used in the testsuite only.


### wsconsume plugin parameters
The wsconsume plugin has the following parameters:

| Attribute | Description | Default |
|-----------|-------------|---------|
| bindingFiles | JAXWS or JAXB binding file | true |
| classpathElements | Each classpathElement provides a library file to be added to classpath | ${project.compileClasspathElements} or ${project.testClasspathElements} |
| catalog | Oasis XML Catalog file for entity resolution | none |
| targetPackage | The target Java package for generated code. | generated |
| bindingFiles | One or more JAX-WS or JAXB binding file | none |
| wsdlLocation | Value to use for @WebServiceClient.wsdlLocation | generated |
| outputDirectory | The output directory for generated artifacts. | ${project.build.outputDirectory} or ${project.build.testOutputDirectory} |
| sourceDirectory | The output directory for Java source. | ${project.build.directory}/generated-sources/wsconsume |
| verbose | Enables more informational output about command progress. | false |
| wsdls | The WSDL files or URLs to consume | n/a |
| extension | Enable SOAP 1.2 binding extension. | false |
| encoding | The charset encoding to use for generated sources. | ${project.build.sourceEncoding} |
| argLine | An optional additional argline to be used when running in fork mode; can be used to set endorse dir, enable debugging, etc. Example <argLine>-Djava.endorsed.dirs=...</argLine> | none |
| fork | Whether or not to run the generation task in a separate VM. | false |
| target | A preference for the JAX-WS specification target | Depends on the underlying stack and endorsed dirs if any |

## wsprovide configuration

### wsproduce maven Goals
| goal | comment |
|------|---------|
|wsproduce | triggers the sources generation during __process-classes__ phase |
|wsproduce-test | triggers the sources generation during __process-test-classes__ phase |


### wsprovide maven plugin example

In this example the plugin is configured to run during the generate-sources phase.
It produces the wsdl file and artifact sources for the specified endpoint class,
TestEndpoint.  By changing the goal to wsprovide-test the same files will be
generated during the generate-test-sources phase.

```xml
<build>
  <plugins>
    <plugin>
      <groupId>org.jboss.ws.plugins</groupId>
      <artifactId>jaxws-tools-maven-plugin</artifactId>
      <version>1.3.0.Final</version>
      <configuration>
        <verbose>true</verbose>
        <endpointClass>org.jboss.test.ws.plugins.tools.wsprovide.TestEndpoint</endpointClass>
        <generateWsdl>true</generateWsdl>
      </configuration>
      <executions>
        <execution>
          <goals>
            <goal>wsprovide</goal>
          </goals>
        </execution>
      </executions>
    </plugin>
  </plugins>
</build>
```


### wsprovide plugin parameters
The wsprovide plugin has the following parameters:

| Attribute | Description | Default |
|-----------|-------------|---------|
| testClasspathElements | Each classpathElement provides a library file to be added to classpath | ${project.compileClasspathElements} or ${project.testClasspathElements} |
| outputDirectory | The output directory for generated artifacts. | ${project.build.outputDirectory} or ${project.build.testOutputDirectory} |
| resourceDirectory | The output directory for resource artifacts (WSDL/XSD). | ${project.build.directory}/wsprovide/resources |
| sourceDirectory | The output directory for Java source. | ${project.build.directory}/wsprovide/java |
| extension | Enable SOAP 1.2 binding extension. | false |
| generateWsdl | Whether or not to generate WSDL. | false |
| verbose | Enables more informational output about command progress. | false |
| portSoapAddress | The generated port soap:address in the WSDL | default |
| endpointClass | Service Endpoint Implementation. | default |


## Special note
Be careful when using this plugin with the Maven War Plugin.  The War plugin
includes project dependency archives in the generated application war. It
is undesirable for org.jboss.ws.cxf:jbossws-cxf-client to be included
in the war file.  To avoid this declare the jbossws-cxf-client dependency
scope as provided, (e.g. ``` <scope>provided</scope>```).




