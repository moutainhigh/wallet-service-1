package io.jingwei.wallet.biz.service.impl;

import io.jingwei.wallet.biz.entity.EthAddress;
import io.jingwei.wallet.biz.mapper.EthAddressMapper;
import io.jingwei.wallet.biz.service.IEthAddressService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author Andy
 * @since 2020-09-27
 */
@Service
public class EthAddressServiceImpl extends ServiceImpl<EthAddressMapper, EthAddress> implements IEthAddressService {

}
