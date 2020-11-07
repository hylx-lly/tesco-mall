package com.jerusalem.order.service.impl;

import com.jerusalem.cart.feign.CartFeign;
import com.jerusalem.common.vo.OrderItemVo;
import com.jerusalem.common.vo.UserAddressVo;
import com.jerusalem.common.vo.UserResponseVo;
import com.jerusalem.order.interceptor.LoginInterceptor;
import com.jerusalem.order.vo.OrderConfirmVo;
import com.jerusalem.user.feign.UserReceiveAddressFeign;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jerusalem.common.utils.PageUtils;
import com.jerusalem.common.utils.Query;

import com.jerusalem.order.dao.OrdersDao;
import com.jerusalem.order.entity.OrdersEntity;
import com.jerusalem.order.service.OrdersService;

/****
 * 服务层接口实现类
 * 订单
 * @author jerusalem
 * @email 3276586184@qq.com
 * @date 2020-04-09 17:49:06
 */
@Service("ordersService")
public class OrdersServiceImpl extends ServiceImpl<OrdersDao, OrdersEntity> implements OrdersService {

    @Autowired
    UserReceiveAddressFeign userReceiveAddressFeign;

    @Autowired
    CartFeign cartFeign;

    /**
    * 分页查询
    * @param params
    * @return
    */
    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<OrdersEntity> page = this.page(
                new Query<OrdersEntity>().getPage(params),
                new QueryWrapper<OrdersEntity>()
        );
        return new PageUtils(page);
    }

    /***
     * 获取结算页封装信息
     * @return
     */
    @Override
    public OrderConfirmVo confirmOrder() {
        OrderConfirmVo orderConfirmVo = new OrderConfirmVo();
        UserResponseVo userResponseVo = LoginInterceptor.loginUser.get();
        //1.远程查询用户的所有收货地址信息
        List<UserAddressVo> addressList = userReceiveAddressFeign.getAddress(userResponseVo.getId());
        orderConfirmVo.setUserAddressVos(addressList);
        //2.远程查询购物车选中的购物项
        //feign远程调用请求头丢失问题：添加拦截器，通信新老请求数据
        List<OrderItemVo> userCartItems = cartFeign.getUserCartItems();
        orderConfirmVo.setOrderItemVos(userCartItems);
        //3.查询用户积分
        Integer integration = userResponseVo.getIntegration();
        orderConfirmVo.setIntegration(integration);
        //4.其他数据自动计算
        //TODO 5.防重令牌
        return orderConfirmVo;
    }
}