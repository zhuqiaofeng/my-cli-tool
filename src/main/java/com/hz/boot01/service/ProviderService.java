package com.hz.boot01.service;

import com.hz.boot01.pojo.Provider;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 供应商业务接口
 */
public interface ProviderService {
    /**
     * 根据ID  获得供应商对象
     * @param id
     * @return
     */
    public Provider findProviderById(Integer id);

    /**
     * 多条件组合查询
     * 翻页查询供应商信息
     * @param curr_page  当前页
     * @param page_size 每页显示条数
     * @return
     */
    public List<Provider> findProviderList(
         Integer curr_page, Integer page_size,
         String proName,String proDesc
    );
    /**
     * 根据ID  删除供应商
     * @param id
     * @return
     */
    public int deleteProviderById(Integer id);

    /**
     * 修改供应商信息
     * @param provider
     * @return
     */
    public int updateProvider(Provider provider);

    /**
     * 新增供应商信息
     * @param provider
     * @return
     */
    public int saveProvider(Provider provider);


    /**
     * 获得总条数
     * @return
     */
    public Integer findProviderListCount(String proName,String proDesc);

}
