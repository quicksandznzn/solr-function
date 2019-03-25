package org.quicksandzn.solr.function;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.lucene.index.LeafReaderContext;
import org.apache.lucene.queries.function.FunctionValues;
import org.apache.lucene.queries.function.ValueSource;
import org.apache.lucene.queries.function.docvalues.DoubleDocValues;

/**
 * tag_match函数具体实现
 *
 * 用法 tag_match(query_key, doc_field, kv_op, merge_op)
 *
 */
public class TagMatchValueSource extends ValueSource {

    /**
     * queryKey 格式10=0.67:960=0.85:1=48
     */
    private Map<String, Double> queryKey;
    /**
     * 单个字段 字段内容格式 1 2 2 1 数组形式
     */
    private ValueSource docField;
    /**
     * 支持 sum（求和）、avg（平均数）、mul（乘积）、max（最大值）、min（最小值）
     */
    private String kvOp;
    /**
     * 支持 sum（求和）、avg（平均值）、max（最大值）、min（最小值）
     */
    private String mergeOp;

    public TagMatchValueSource() {
    }

    public TagMatchValueSource(Map<String, Double> queryKey, ValueSource docField, String kvOp,
        String mergeOp) {
        this.queryKey = queryKey;
        this.docField = docField;
        this.kvOp = kvOp;
        this.mergeOp = mergeOp;
    }

    /**
     * 转换docField为Map Map<String, Integer> 奇数为key偶数为value
     *
     * @param docField 单个字段 字段内容格式 1 2 2 1 数组形式
     * @return Map<String,Integer>
     */
    private static Map<String, Integer> getDocField(String docField) {
        Map<String, Integer> docMap = new HashMap<String, Integer>();
        String[] arr = docField.split(" ");
        for (int i = 0; i < arr.length; i = i + 2) {
            String k = arr[i];
            String v = arr[i + 1];
            if (null == k || "".equals(k) || null == v || "".equals(v)) {
                continue;
            }
            docMap.put(k, (int) (Double.parseDouble(v)));
        }
        return docMap;
    }


    @Override
    public boolean equals(Object o) {
        return false;
    }

    @Override
    public int hashCode() {
        return 0;
    }

    @Override
    public String description() {
        return "This is TagMatch";
    }

    @Override
    public FunctionValues getValues(Map map, LeafReaderContext leafReaderContext)
        throws IOException {
        final FunctionValues docItem = docField.getValues(map, leafReaderContext);
        return new DoubleDocValues(this) {
            @Override
            public double doubleVal(int i) {
                try {
                    // doc value
                    Object docObj = docItem.objectVal(i);
                    if (null == docObj) {
                        return 0;
                    }
                    String docVal = String.valueOf(docObj);
                    if ("".equals(docVal)) {
                        return 0;
                    }
                    Map<String, Integer> docFieldMap = getDocField(docVal);
                    //queryKey和docField匹配
                    Map<String, Double> resultMap = getResult(queryKey, docFieldMap, kvOp);
                    return getResult(resultMap, mergeOp);
                } catch (Exception e) {
                    return 0;
                }

            }
        };

    }

    /**
     * 取得最大值
     *
     * @param list 结果集
     * @return Double
     */
    private Double getMax(List<Double> list) {
        double maxIndex = list.get(0);
        for (int i = 0; i < list.size(); i++) {
            if (maxIndex < list.get(i)) {
                maxIndex = list.get(i);
            }
        }

        return maxIndex;
    }

    /**
     * 取得最小值
     *
     * @param list 结果集
     * @return Double
     */
    private Double getMin(List<Double> list) {
        double minIndex = list.get(0);
        for (int i = 0; i < list.size(); i++) {
            if (minIndex > list.get(0)) {
                minIndex = list.get(0);
            }
        }
        return minIndex;
    }

    /**
     * 遍历所有key的value的结果集根据函数进行操作返回分值
     *
     * @param resultMap 所有key和value的结果集
     * @param mergeOp 具体操作函数
     * @return Double
     */
    private Double getResult(Map<String, Double> resultMap, String mergeOp) {
        Double result = 0D;
        if (null == resultMap || resultMap.isEmpty() || null == mergeOp || "".equals(mergeOp)) {
            return result;
        }
        List<Double> list = new ArrayList<>();
        switch (mergeOp) {
            case "sum":
                for (Map.Entry<String, Double> kv : resultMap.entrySet()) {
                    Double value = kv.getValue();
                    result += value;
                }
                break;
            case "avg":
                for (Map.Entry<String, Double> kv : resultMap.entrySet()) {
                    Double value = kv.getValue();
                    result += value;
                }
                result = result / resultMap.size();
                break;
            case "max":
                for (Map.Entry<String, Double> kv : resultMap.entrySet()) {
                    Double value = kv.getValue();
                    list.add(value);
                }
                result = getMax(list);
                break;
            case "min":
                for (Map.Entry<String, Double> kv : resultMap.entrySet()) {
                    Double value = kv.getValue();
                    list.add(value);
                }
                result = getMin(list);
                break;
            default:
                break;

        }

        return result;
    }

    /**
     * queryKey和docField匹配后取得每个key和每个key对应的value
     *
     * @param queryKey queryKey
     * @param docField docField
     * @param kvOp 具体操作函数
     * @return Map<Integer,Double>
     */
    private Map<String, Double> getResult(Map<String, Double> queryKey,
        Map<String, Integer> docField, String kvOp) {
        if (null == docField || docField.isEmpty() || null == queryKey || queryKey.isEmpty()
            || null == kvOp || "".equals(kvOp)) {
            return null;
        }
        Map<String, Double> map = new HashMap<>();
        switch (kvOp) {
            case "sum":
                for (Map.Entry<String, Double> qk : queryKey.entrySet()) {
                    String key = qk.getKey();
                    if (docField.containsKey(key)) {
                        Double result = qk.getValue() + docField.get(key);
                        map.put(key, result);
                    }
                }

                break;
            case "avg":
                for (Map.Entry<String, Double> qk : queryKey.entrySet()) {
                    String key = qk.getKey();
                    if (docField.containsKey(key)) {
                        Double result = (qk.getValue() + docField.get(key)) / 2;
                        map.put(key, result);
                    }
                }
                break;
            case "mul":

                for (Map.Entry<String, Double> qk : queryKey.entrySet()) {
                    String key = qk.getKey();

                    if (docField.containsKey(key)) {
                        Double result = qk.getValue() * docField.get(key);
                        map.put(key, result);
                    }
                }
                break;
            case "max":
                for (Map.Entry<String, Double> qk : queryKey.entrySet()) {
                    String key = qk.getKey();
                    if (docField.containsKey(key)) {
                        Double result = (qk.getValue() > docField.get(key) ? qk.getValue()
                            : docField.get(key).doubleValue());
                        map.put(key, result);
                    }
                }
                break;
            case "min":
                for (Map.Entry<String, Double> qk : queryKey.entrySet()) {
                    String key = qk.getKey();
                    if (docField.containsKey(key)) {
                        Double result = (qk.getValue() > docField.get(key) ? docField.get(key).doubleValue()
                            : qk.getValue());
                        map.put(key, result);
                    }
                }
                break;
            default:
                break;

        }

        return map;
    }

}
