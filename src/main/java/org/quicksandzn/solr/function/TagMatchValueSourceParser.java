package org.quicksandzn.solr.function;

import java.util.HashMap;
import java.util.Map;
import org.apache.lucene.queries.function.ValueSource;
import org.apache.solr.schema.SchemaField;
import org.apache.solr.search.FunctionQParser;
import org.apache.solr.search.SyntaxError;
import org.apache.solr.search.ValueSourceParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * tag_match函数入口类
 *
 * 用法 tag_match(query_key, doc_field, kv_op, merge_op)
 */
public class TagMatchValueSourceParser extends ValueSourceParser {

    private static final Logger LOG = LoggerFactory.getLogger(TagMatchValueSourceParser.class);

    @Override
    public ValueSource parse(FunctionQParser fqp) throws SyntaxError {
        //  queryKey 格式10=0.67:960=0.85:1=48
        String queryKey = fqp.parseArg();
        // 单个字段 字段内容格式 1 2 2 1 数组形式
        String docField = fqp.parseArg();
        // 支持 sum（求和）、avg（平均数）、mul（乘积）、max（最大值）、min（最小值）
        String kvOp = fqp.parseArg();
        // 支持 sum（求和）、avg（平均值）、max（最大值）、min（最小值）
        String mergeOp = fqp.parseArg();

        return new TagMatchValueSource(getQueryKey(queryKey), getValueSource(fqp, docField), kvOp,
            mergeOp);
    }

    /**
     * 解析queryKey为map
     *
     * @param queryKey queryKey 格式10=0.67:960=0.85:1=48
     * @return Map<String               ,                               Double>
     */
    private Map<String, Double> getQueryKey(String queryKey) {
        Map<String, Double> qkMap = new HashMap<String, Double>();
        String[] arr = queryKey.split(":");
        for (String qks : arr) {
            if (null == qks || "".equals(qks)) {
                continue;
            }
            String[] qk = qks.split("=");
            String k = qk[0];
            String v = qk[1];
            if (null == k || "".equals(k) || null == v || "".equals(v)) {
                continue;
            }
            qkMap.put(k, Double.parseDouble(v));
        }
        return qkMap;
    }


    /**
     * 该方法是根据字段名，从FunctionQParser得到文档该字段的相关信息
     *
     * @param fp FunctionQParser
     * @param arg 字段名
     * @return ValueSource
     */
    private ValueSource getValueSource(FunctionQParser fp, String arg) {
        if (null == arg) {
            return null;
        }
        SchemaField f = fp.getReq().getSchema().getField(arg);

        return f.getType().getValueSource(f, fp);
    }

}
