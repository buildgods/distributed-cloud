package org.cloud.common.utils;

import java.util.UUID;

/**
 * 邮箱验证码生成工具
 */
public class CodeGeneratorUtil {
    /**
     * 生成指定长度的验证码
     * @param length 长度
     * @return
     */
    public static String generateCode(int length){
        return UUID.randomUUID().toString().substring(0, length);
    }

}
