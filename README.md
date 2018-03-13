## What is BlueNimble?

BlueNimble is a Hybrid Serverless Platform, focusing on developer productivity and application portability.

BlueNimble is a new and simple way to create and run applications with no specific binding to a clustering or microservices technology, making it pain-free to embrace future technologies and deployment models.

Using BlueNimble, Developers focus on coding application business logic, without any knowledge of the microservices architecture.

## Quick Start - Single Node

### Install Java 8 or higher
 * On Mac, Windows or Linux, install the latest [OpenJDK JRE](http://openjdk.java.net) or [ORACLE JRE](http://www.oracle.com/technetwork/java/javase/downloads). Previous versions to Java 8 are not supported.

### Install BlueNimble

#### Install from binaries
* Download either [bluenimble-1.0.1-bin.zip](https://github.com/bluenimble/serverless/releases/download/v1.0.1/bluenimble-1.0.1-bin.zip) or [bluenimble-1.0.1-bin.tar.gz](https://github.com/bluenimble/serverless/releases/download/v1.0.1/bluenimble-1.0.1-bin.tar.gz) and decompress in a folder of your choice
* If you're a Mac or Linux user, set the right .sh file mode:
    ````
    cd ~/bluenimble-1.0.0 && sudo chmod 755 *.sh
    ````
#### Start BlueNimble 
* Mac or Linux users
    ````
    ./bnb.sh
    ````
* Windows users
    ````
    ./bnb.bat
    ````
#### Install CLI from binaries
* Download either [bluenimble-cli-1.0.1-bin.zip](https://github.com/bluenimble/serverless/releases/download/v1.0.1/bluenimble-cli-1.0.1-bin.zip) or [bluenimble-cli-1.0.1-bin.tar.gz](https://github.com/bluenimble/serverless/releases/download/v1.0.1/bluenimble-cli-1.0.1-bin.tar.gz) and decompress in a folder of your choice
* If you're a Mac or Linux user, set the right .sh file mode:
    ````
    cd ~/blueNimble-cli-1.0.0 && chmod 755 *.sh
    ````
The CLI could be installed in any other machine, not necessarily where the server is installed 

#### Start CLI 
* Mac or Linux users
    ````
    ./bnb.sh
    ````
* Windows users
    ````
    ./bnb.bat
    ````
#### Install from sources
* Install git and maven 
* Clone the [blunimble/serverless repository](http://github.com/bluenimble/serverless) from Github. 
    * On the command line, enter:
    ````
    git clone https://github.com/bluenimble/serverless.git
    ````
    * You can probably use [Git for Windows](http://windows.github.com/) or [Git for Mac](http://mac.github.com/) instead of the command line, however these aren't tested/supported and we only use the command line for development.
* Build binaries
    * On the command line, enter:
    ````
    mvn install
    ````
This command will build both BlueNimble Server and the CLI 

### Check Server startup and install security keys
#### Check Server startup
Type in http://server-ip:8080 (server-ip is where you installed bluenimble) or localhost if you're in your laptop. If you see a page similar to the one below, then BlueNimble is up and running.   

![BlueNimble Server Install Page](https://github.com/bluenimble/serverless/blob/master/assets/images/bluenimble-install-short.png)

#### Download and Install security keys
By default, BlueNimble's built with a playground space. 
* From the install page, click the green button associated with the playground space to download the security keys.
* In order to install playground.keys into the CLI, type in:  
    ````sql
    load keys path_to/playground.keys
    ````
* You can, eventually, check if the keys were installed, by issuing:   
    ````sql
    keys
    ````

#### Create your first api
* Let's create an uber api. To do so, type in:  
    ````sql
    create api uber
    ````
This command should create the api project in your local machine.
* Let's create a service. Type in:  
    ````sql
    create service * driver
    ````
This command will create 5 services for the model 'driver' corresponding to 'create', 'update', 'delete', 'get' and 'find'.
* We are good to run the uber api. To do so, type in:
    ````sql
    push api uber
    ````
* Try it out. Open up your browser and type in http://server-ip:8080/playground/uber/drivers/unknown, this request will call the service GET /uber/drivers. Or use curl or a GUI tool such as postman to send post and put requests 
* Access the uber api sources, make some changes to the code, run "push api uber" to try it again. The api sources are located under the CLI workspace, type in: 
    ````sql
    ws
    ````
This command will print out where your api code is stored. You can change the workspace folder by issuing:  

    ws pathToNewFolder
    
From now on, any api you create, will be stored in this folder. 

## Terminology

* BlueNimble can act as an Api Gateway and an Execution Runtime or both. In the single node setup we did, we are running Bluenimble for both. 

* BlueNimble runs a set of Spaces. Each space defines a set of features to use and accessible by the Apis you will push to it. For example, if a space defines a database feature, all Apis, thus the functions deployed to it, will share this same database instance.

* An Api is a set of services and corresponding functions. An Api may also define which security scheme to be used, tracing (logging) and requests tracking. An Api could eventually be pushed to multiple spaces (Dev, QA, Prod, ...) since the only dependency is the set of features this api is using. 

* A Service is a an interface specification which is defined by the service.json file. A service can define validation rules to apply on requests, specific security and eventually an SPI function (Service Provider Implementation)

* Plugins are one of the most important components of the BlueNimble architecture when it comes to application portability. Plugins aren't just extensions, such as supporting a new feature, but they can change the behaviour of anything happening in the server. <br/>
You can create plugins to accept requests through a new network protocol such as COAP, to support new security mechanisms, change the flow of an incoming request, etc. <br/>
Plugins also receive events of changes happening to a space or an api. For example, the Kubernetes or Swarm plugins intercept the "`Push Api`" event to push to the cluster, they also change services SPI fuctions to delegate load to the cluster instead of the Api Gateway.   
  
## Architecture

### High Level Flow Diagram
The figure below is a hight level flow diagram

![BlueNimble Hight-Level Flow-architecture](https://github.com/bluenimble/serverless/blob/master/assets/images/main-opt.png)

### Application Portability  
Even if developers can use any external library in their functions code. We recommend to use assets through the **features/plugins** interfaces. For example, the datasource plugin provides native support to a number of relational databases, you can add a new one to the plugin by only registring the vendor and it's driver. This will free developers from managing security and opening/recycling/pooling of connections. 

Here is the list of the out-of-the-box features:

- Database: For document databases. Mongodb, Couchdb and Orientdb supported out-of-the-box. You can add other vendors by implementing the **Database Feature** plugin. 

- Datasource: Includes most known relational databases, cassandra, salesforce. Hive and Hbase could be added by adding a new vendor to the plugin.

- Storage: Supports FileSystem - S3 and other blob storage services could be used through a posix compliant interface.

- Messenger: Supports Mail and Mobile Push Notifications. Other vendors such as STOMP, APMQ could be added by implementing the Messenger feature plugin.

- Indexer: Only ElasticSearch is supported. You can implement your own Indexer using feature plugin.

- Remoting: Supports only HTTP to integrate with http-aware services. You can implement your own Remoting using feature plugin.

## Documentation
Visit the [Developer Guide](https://www.bluenimble.com/devcommunity.html), [CLI Guide](https://www.bluenimble.com/icli.html) and [SDK Reference Guide](https://www.bluenimble.com/docs/apiref/js/index.html) for more details.


License
=======
Copyright 2018 BlueNimble, Inc.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
