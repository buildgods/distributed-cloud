package org.cloud.article.validation;


import org.cloud.article.ano.State;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

/**
 * <State,String> 第一个参数是要使用的注解，第二个参数为要检验字段的类型
 */
public class StateValidation implements ConstraintValidator<State,String> {
    /**
     *
     * @param s 将来要校验的拉数据
     * @param constraintValidatorContext
     * @return
     */
    @Override
    public boolean isValid(String s, ConstraintValidatorContext constraintValidatorContext) {
        // 提供校验规则
        if(s == null)
        {
            return false;
        }else if(s.equals("已发布") || s.equals("草稿")){
            return true;
        }
        return false;
    }
}
