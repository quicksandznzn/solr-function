# solr-function

Custom Solr FunctionQueries

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
4. edit solr.in.sh
/usr/local/Cellar/solr/8.0.0/share/solr/solr.in.sh
```js
ZK_HOST="127.0.0.1:2181/solr"
SOLR_HOST="127.0.0.1"
SOLR_TIMEZONE="UTC+8"
```
4. restart solr use cloud mode
    
   solr restart -c -p 8888  
   solr restart -c -p 8889

