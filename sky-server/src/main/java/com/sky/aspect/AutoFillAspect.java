package com.sky.aspect;

import com.sky.annotation.AutoFill;
import com.sky.constant.AutoFillConstant;
import com.sky.context.BaseContext;
import com.sky.enumeration.OperationType;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.time.LocalDateTime;

/**
 * 自动填充切面类，实现公共自动填充
 */
@Aspect
@Component
@Slf4j
public class AutoFillAspect {
    // 切入点
    @Pointcut("execution(* com.sky.mapper.*.*(..)) && @annotation(com.sky.annotation.AutoFill)")
    public void autoFillPointCut() {

    }

    /**
     * 自定义前置通知
     * @param{JoinPoint} joinPoint
     */
    @Before("autoFillPointCut()")
    public void autoFill(JoinPoint joinPoint) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        log.info("开始进行公共字段自动填充");

        // 获取操作类型
        MethodSignature signature = (MethodSignature) joinPoint.getSignature(); // 方法签名对象
        AutoFill autoFill = signature.getMethod().getAnnotation(AutoFill.class); // 获得方法上的注解对象
        OperationType operationType = autoFill.value(); // 获取数据库实际操作类型
        
        // 获取拦截的方法参数 -- 实体对象
        Object[] args = joinPoint.getArgs(); // 获取所有参数
        if (args.length == 0) {
            return;
        }
        
        Object entity = args[0]; // 获取第一个实体
        
        // 准备赋值的数据
        LocalDateTime now = LocalDateTime.now();
        Long currentId = BaseContext.getCurrentId();
        
        // 根据不同的操作类型，为对应的字段赋值
        if (operationType == OperationType.INSERT) {
            // 为4个公共字段赋值
            Method setUpdateTime = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_TIME, LocalDateTime.class);
            Method setUpdateUser = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_USER, Long.class);
            Method setCreateTime = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_CREATE_TIME, LocalDateTime.class);
            Method setCreateUser = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_CREATE_USER, Long.class);

            // 通过反射赋值
            setUpdateTime.invoke(entity, now);
            setUpdateUser.invoke(entity, currentId);
            setCreateTime.invoke(entity, now);
            setCreateUser.invoke(entity, currentId);
        } else if (operationType == OperationType.UPDATE) {
            Method setUpdateTime = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_TIME, LocalDateTime.class);
            Method setUpdateUser = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_USER, Long.class);

            setUpdateTime.invoke(entity, now);
            setUpdateUser.invoke(entity, currentId);
        }
        
    }
}
