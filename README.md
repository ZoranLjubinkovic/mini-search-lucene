# Mini-search Lucene

This project uses Quarkus, Lucene
and combinatoricslib3 (for Permutations ) <https://github.com/dpaukov/combinatoricslib3>

## 'Software engineer to Search engineer' course.

## All tests:

```shell
 ./mvnw clean test
```

## Run main class:

```shell
 ./mvnw quarkus:run
```

## Open <http://localhost:8080/q/swagger-ui/>

---
### Packaging the application

The application can be packaged using:

```shell
 ./mvnw package
```


```shell
 DOCKER_DEFAULT_PLATFORM="linux/amd64"  \
 docker build -f src/main/Docker/Dockerfile.jvm \
 -t europe-west6-docker.pkg.dev/gd-gcp-em-search-re-training/trainingartifacts/zlj-lucene-mini_search:0.1 .
```

```shell
 docker run -i --rm -p 8080:8080 \
 europe-west6-docker.pkg.dev/gd-gcp-em-search-re-training/trainingartifacts/zlj-lucene-mini_search:0.1
```



## Open [http://localhost:8080/q/swagger-ui/](http://localhost:8080/q/swagger-ui/)

---
### Deploy to GCP


```shell
 gcloud auth login
```
```shell
 gcloud config set project gd-gcp-em-search-re-training
```
```shell
 gcloud auth configure-docker europe-west6-docker.pkg.dev
```
```shell
 docker push \
 europe-west6-docker.pkg.dev/gd-gcp-em-search-re-training/trainingartifacts/zlj-lucene-mini_search:0.1
```


# Open <https://zlj-lucene-mini-search-gixfoxo4pq-ew.a.run.app/q/swagger-ui/>


---
## Author

### Zoran Ljubinković [zljubinkovic@griddynamics.com](mailto:zljubinkovic@griddynamics.com)




