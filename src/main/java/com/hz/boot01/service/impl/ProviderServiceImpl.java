package com.hz.boot01.service.impl;

import com.hz.boot01.dao.ProviderDao;
import com.hz.boot01.pojo.Provider;
import com.hz.boot01.service.ProviderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ProviderServiceImpl implements ProviderService {

    @Autowired
    private ProviderDao providerDao;

    @Override
    @Transactional //开启事务
    public Provider findProviderById(Integer id) {

        return providerDao.findProviderById(id);
    }

    @Override
    public List<Provider> findProviderList(Integer curr_page, Integer page_size,String proName,String proDesc) {
        //计算偏移量 = (当前页-1)*每页显示条数
        Integer pyl = (curr_page-1)*page_size;
        return providerDao.findProviderList(pyl,page_size,proName,proDesc);
    }

    @Override
    public int deleteProviderById(Integer id) {
        return providerDao.deleteProviderById(id);
    }

    @Override
    public int updateProvider(Provider provider) {
        return providerDao.updateProvider(provider);
    }

    @Override
    public int saveProvider(Provider provider) {

        return providerDao.saveProvider(provider);
    }

    @Override
    public Integer findProviderListCount(String proName,String proDesc) {
        return providerDao.findProviderListCount(proName,proDesc);
    }
}
