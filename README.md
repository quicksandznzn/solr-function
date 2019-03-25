# solr-function

solr version 8.0.0

1. mvn clean install

```js
2. put jar to path 
```
3. solrconfig.xml
```xml
<lib dir="path" regex="solr-function.jar"/>
<valueSourceParser class="org.quicksandzn.solr.function.TagMatchValueSourceParser" name="tag_match"/>
```
4. solr restart -c -p 8888

