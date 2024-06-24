package org.cloud.article.ano;



import org.cloud.article.validation.UrlStateValidation;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Documented// 元注解
@Constraint(
        validatedBy = {UrlStateValidation.class} // 提供指定校验规则的类
)
@Target({FIELD})// 元注解
@Retention(RUNTIME)// 元注解 运行阶段
public @interface UrlState {
    // 检验失败的提示信息
    String message() default "url_state参数的值只能是有或者无";
    // 指定分组
    Class<?>[] groups() default {};
    // 负载 获取到State注解的附加条件
    Class<? extends Payload>[] payload() default {};
}
