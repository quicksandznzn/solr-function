# solr-function

solr version 7.1.0

1. mvn clean install

2. put jar to <font color="#dd0000">path</font>

3. solrconfig.xml
```xml
<lib dir="path" regex="solr-function.jar"/>
<valueSourceParser class="org.quicksandzn.solr.function.TagMatchValueSourceParser" name="tag_match"/>
```
4. brew services restart elasticsearch@5.6

4. 