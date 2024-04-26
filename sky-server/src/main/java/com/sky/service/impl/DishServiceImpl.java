package com.sky.service.impl;

import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.constant.StatusConstant;
import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.entity.DishFlavor;
import com.sky.exception.DeletionNotAllowedException;
import com.sky.mapper.DishFlavorMapper;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetmealDishMapper;
import com.sky.result.PageResult;
import com.sky.service.DishService;
import com.sky.vo.DishVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

@Service
@Slf4j
public class DishServiceImpl implements DishService {
    @Autowired
    private DishMapper dishMapper;
    @Autowired
    private DishFlavorMapper dishFlavorMapper;
    @Autowired
    private SetmealDishMapper setmealDishMapper;

    /**
     * 新增菜品以及保存口味
     *
     * @param dishDTO
     */
    @Override
    public void saveWithFlavor(DishDTO dishDTO) {
        Dish dish = new Dish();
        BeanUtils.copyProperties(dishDTO, dish);

        // 向菜品表插入一条记录
        dishMapper.insert(dish);

        // 获取insert生成的主键id
        Long dishId = dish.getId();

        List<DishFlavor> flavors = dishDTO.getFlavors();
        if (flavors != null && !flavors.isEmpty()) {
            for (DishFlavor flavor : flavors) {
                // 向口味表插入n个数据
                flavor.setDishId(dishId);
            }
            dishFlavorMapper.insertBatch(flavors);
        }
    }

    /**
     * 菜品分页查询
     *
     * @param dishPageQueryDTO
     * @return
     */
    @Override
    public PageResult pageQuery(DishPageQueryDTO dishPageQueryDTO) {
        PageHelper.startPage(dishPageQueryDTO.getPage(), dishPageQueryDTO.getPageSize());

        List<DishVO> dishVOList = dishMapper.pageQuery(dishPageQueryDTO);

        PageResult pageResult = new PageResult();
        pageResult.setTotal(dishVOList.size());
        pageResult.setRecords(dishVOList);

        return pageResult;
    }

    /**
     * 分页查询菜品
     *
     * @param dishPageQueryDTO
     */
    // @Override
    // public PageResult<DishVO> queryPage(DishPageQueryDTO dishPageQueryDTO) {
    //     return null;
    // }

    /**
     * 删除菜品
     *
     * @param ids
     */
    @Override
    // 事务一致性
    @Transactional
    public void deleteBatch(Long[] ids) {
        // 判断菜品是否被套餐关联
        for (Long id: ids) {
            Dish dish = dishMapper.getById(id);
            if (Objects.equals(dish.getStatus(), StatusConstant.ENABLE)) {
                // 正在销售的菜品不能删除
                throw new DeletionNotAllowedException(MessageConstant.DISH_ON_SALE);
            }
        }

        // 判断菜品是否被套餐关联
        List<Long> setmealIdsByDishIds = setmealDishMapper.getSetmealIdsByDishIds(Arrays.asList(ids));
        if (setmealIdsByDishIds != null && !setmealIdsByDishIds.isEmpty()) {
            // 菜品被套餐关联，不能删除
            throw new DeletionNotAllowedException(MessageConstant.DISH_BE_RELATED_BY_SETMEAL);
        }

        // 删除菜品表的菜品数据
        for (Long id: ids) {
            dishMapper.deleteById(id);

            // 删除菜品关联的口味数据
            dishFlavorMapper.deleteByDishId(id);
        }
    }

    /**
     * 根据id查询菜品
     *
     * @param id
     * @return
     */
    @Override
    public DishVO getByIdWithFlavor(Long id) {
        Dish dish = dishMapper.getById(id);
        DishVO dishVO = new DishVO();

        if (dish != null) {
            BeanUtils.copyProperties(dish, dishVO);
            // 查询菜品对应的口味数据，从dish_flavor表查询
            List<DishFlavor> flavors = dishFlavorMapper.getByDishId(id);
            dishVO.setFlavors(flavors);
        }

        return dishVO;
    }

    /**
     * 更新菜品以及口味
     *
     * @param dishDTO
     */
    @Override
    public void updateWithFlavor(DishDTO dishDTO) {
        Dish dish = new Dish();
        BeanUtils.copyProperties(dishDTO, dish);

        // 修改菜品表基本信息
        dishMapper.update(dish);

        // 删除原有口味数据
        dishFlavorMapper.deleteByDishId(dish.getId());

        List<DishFlavor> flavors = dishDTO.getFlavors();
        if (flavors != null && !flavors.isEmpty()) {
            // 批量插入n条数据
            flavors.forEach(flavor -> {
                flavor.setDishId(dish.getId());
            });
            dishFlavorMapper.insertBatch(flavors);
        }
    }

    /**
     * 启用或禁用菜品
     *
     * @param status
     * @param id
     */
    @Override
    public void startOrStop(Integer status, Long id) {

    }

    /**
     * 根据分类id查询菜品
     *
     * @param categoryId
     * @return
     */
    @Override
    public List<Dish> list(Long categoryId) {
        return dishMapper.getListById(categoryId);
    }

    /**
     * 条件查询菜品和口味
     * @param dish
     * @return
     */
    public List<DishVO> listWithFlavor(Dish dish) {
        List<Dish> dishList = dishMapper.list(dish);

        List<DishVO> dishVOList = new ArrayList<>();

        for (Dish d : dishList) {
            DishVO dishVO = new DishVO();
            BeanUtils.copyProperties(d,dishVO);

            //根据菜品id查询对应的口味
            List<DishFlavor> flavors = dishFlavorMapper.getByDishId(d.getId());

            dishVO.setFlavors(flavors);
            dishVOList.add(dishVO);
        }

        return dishVOList;
    }

    /**
     * 根据分类id查询菜品
     *
     * @param categoryId
     * @return
     */
    // @Override
    // public List<Dish> list(Long categoryId) {
    //     // return List.of();
    // }

    /**
     * 条件查询菜品和口味
     *
     * @param dish
     * @return
     */
    // @Override
    // public List<DishVO> listWithFlavor(Dish dish) {
    //     // return List.of();
    // }
}
