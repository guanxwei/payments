package org.wgx.payments.tools;

import java.lang.reflect.Method;
import java.sql.Date;
import java.sql.Timestamp;
import java.util.Random;

import org.apache.commons.lang3.RandomStringUtils;

/**
 * Utility class to help construct object with random properties.
 * The only requirement on the object to be generated is that the classes
 * should contain a default constructor and standard setter methods;
 *
 * Currently we do not support recursion initiation.
 *
 * Please do not use this in product environment, cause it is designed for test cases only.
 *
 * @author hzweiguanxiong
 *
 */
public final class ObjectGenerator<T> {

    private static final Random RANDOM = new Random(System.nanoTime());

    public static <T> T generate(final Class<T> clazz) throws Exception {
        T o = clazz.newInstance();
        Method[] methods = clazz.getDeclaredMethods();
        for (Method method : methods) {
            if (method.getName().startsWith("set")) {
                Class<?> parameterType = method.getParameterTypes()[0];
                if (parameterType.equals(String.class)) {
                    method.invoke(o, RandomStringUtils.randomAlphabetic(10));
                    continue;
                } else if (parameterType.equals(Integer.class) || parameterType.equals(int.class)) {
                    method.invoke(o, RANDOM.nextInt());
                    continue;
                } else if (parameterType.equals(Boolean.class)) {
                    method.invoke(o, false);
                    continue;
                } else if (parameterType.equals(Long.class) || parameterType.equals(long.class)) {
                    method.invoke(o, System.nanoTime());
                    continue;
                } else if (parameterType.equals(Double.class) || parameterType.equals(double.class)) {
                    method.invoke(o, RANDOM.nextDouble());
                    continue;
                } else if (parameterType.equals(Float.class) || parameterType.equals(float.class)) {
                    method.invoke(0, RANDOM.nextFloat());
                    continue;
                } else if (parameterType.equals(Short.class) || parameterType.equals(short.class)) {
                    method.invoke(o, Short.MAX_VALUE);
                    continue;
                } else if (parameterType.equals(Timestamp.class)) {
                    method.invoke(o, new Timestamp(System.currentTimeMillis()));
                    continue;
                } else if (parameterType.equals(Date.class)) {
                    method.invoke(o, new Date(System.currentTimeMillis()));
                    continue;
                }
            }
        }
        return o;
    }
}
