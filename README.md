**Environment variable:**
```shell
SPRING_CONFIG_IMPORT=optional:file:./my-config.yaml
```

**Command line:**       
```shell
java -jar app.jar --spring.config.import=optional:file:./my-config.yaml
```

**Docker:**                   
```shell
docker run -e SPRING_CONFIG_IMPORT=optional:file:
/app/config/custom.yaml \                                                                                                                                
-v /my/config:/app/config spektr  
```
 