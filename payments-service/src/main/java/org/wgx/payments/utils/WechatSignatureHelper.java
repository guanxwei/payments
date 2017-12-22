package org.wgx.payments.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

/**
 * WechatSignatureHelper.
 */
public final class WechatSignatureHelper {

    private WechatSignatureHelper() { }

  /**
   * Generate signature.
   * @param map Input values.
   * @param key Key.
   * @return QueryString style string containing signature information.
   */
   public static String getSignWithKey(final Map<String, Object> map, final String key) {
       ArrayList<String> list = new ArrayList<String>();
       for (Map.Entry<String, Object> entry : map.entrySet()) {
           if (!"".equals(entry.getValue())) {
               list.add(entry.getKey() + "=" + entry.getValue() + "&");
           }
       }
       int size = list.size();
       String[] arrayToSort = list.toArray(new String[size]);
       Arrays.sort(arrayToSort, String.CASE_INSENSITIVE_ORDER);
       StringBuilder sb = new StringBuilder();
       for (int i = 0; i < size; i++) {
           sb.append(arrayToSort[i]);
       }
       String result = sb.toString();
       result += "key=" + key;
       result = WechatPayMD5.md5Encode(result).toUpperCase();
       return result;
   }

   /**
    * Verify signature.
    * @param map Input values.
    * @param key Key.
    * @return Verify result.
    */
   public static boolean signVerifyWithKey(final Map<String, Object> map, final String key) {
       String signFromAPIResponse = map.get("sign").toString();
       if (!StringUtils.isNotBlank(signFromAPIResponse)) {
           return false;
       }
       map.put("sign", "");
       String signForAPIResponse = getSignWithKey(map, key);
       if (!signForAPIResponse.equals(signFromAPIResponse)) {
           return false;
       }
       return true;
   }

   /**
    * 把数组所有元素排序，并按照“参数=参数值”的模式用“&”字符拼接成字符串.
    * @param params 需要排序并参与字符拼接的参数组
    * @return 拼接后字符串
    */
   public static String createLinkString(final Map<String, Object> params) {

       List<String> keys = new ArrayList<>(params.keySet());
       Collections.sort(keys);

       StringBuilder prestr = new StringBuilder();

       for (int i = 0; i < keys.size(); i++) {
           String key = keys.get(i);
           String value = (String) params.get(key);
           if (i == keys.size() - 1) {
               prestr.append(key + "=" + value);
           } else {
               prestr.append(key + "=" + value + "&");
           }
       }
       return prestr.toString();
   }

}
