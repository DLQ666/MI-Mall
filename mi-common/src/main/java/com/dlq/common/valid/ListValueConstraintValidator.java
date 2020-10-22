package com.dlq.common.valid;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.HashSet;
import java.util.Set;

/**
 *@program: MI-Mall
 *@description:
 *@author: Hasee
 *@create: 2020-10-21 14:11
 */
public class ListValueConstraintValidator implements ConstraintValidator<ListValue ,Integer> {

    private Set<Integer> set = new HashSet<>();
    //初始化方法
    @Override
    public void initialize(ListValue constraintAnnotation) {
        int[] vals = constraintAnnotation.vals();
        for (int val : vals) {
            set.add(val);
        }
    }

    /**
     * @param integer 需要校验的值
     * @param constraintValidatorContext 校验上下文环境信息
     * @return
     */
    //判断是否校验成功
    @Override
    public boolean isValid(Integer integer, ConstraintValidatorContext constraintValidatorContext) {

        return set.contains(integer);
    }
}
