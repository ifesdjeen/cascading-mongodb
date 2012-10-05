
# Deploying required/missing artifacts:

Download hadoop-streaming assembly from Maven, and put it to your local maven repo:

```shell
mvn deploy:deploy-file -Dfile=./mongo-hadoop-streaming-assembly-1.1.0-SNAPSHOT.jar \
                       -DartifactId=mongo-hadoop-streaming
                       -Dversion=1.0.0-SNAPSHOT
                       -DgroupId=org.mongodb
                       -Dpackaging=jar
                       -DgeneratePom=true
                       -Durl=file:repo
```
