package org.cloud.article.validation;


import org.cloud.article.ano.UrlState;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class UrlStateValidation implements ConstraintValidator<UrlState,String> {
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
        }else if(s.equals("有") || s.equals("无")){
            return true;
        }
        return false;
    }
}
