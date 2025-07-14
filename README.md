# Mini-search Lucene

This project uses Quarkus and Lucene v9.12.1

## 'Software engineer to Search engineer' course.

## All tests:

```shell
 ./mvnw clean test
```


---
### Run quarkus application


```shell
./mvnw clean package quarkus:run
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
 DOCKER_DEFAULT_PLATFORM="linux/amd64"  \
 docker build -f src/main/Docker/Dockerfile.jvm \
 -t europe-west6-docker.pkg.dev/gd-gcp-em-search-re-training/trainingartifacts/zlj-lucene-mini_search:0.1 .
```
```shell
 docker push \
 europe-west6-docker.pkg.dev/gd-gcp-em-search-re-training/trainingartifacts/zlj-lucene-mini_search:0.1
```
Create CloudRun on GCP console and deploy !

# Open <https://zlj-lucene-mini-search-gixfoxo4pq-ew.a.run.app/q/swagger-ui/>


---
## Author

### Zoran Ljubinković [zljubinkovic@griddynamics.com](mailto:zljubinkovic@griddynamics.com)




