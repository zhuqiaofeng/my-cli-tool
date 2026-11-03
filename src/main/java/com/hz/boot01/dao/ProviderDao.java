package com.hz.boot01.dao;

import com.hz.boot01.pojo.Provider;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 供应商dao接口
 */
public interface ProviderDao {

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
     * 多条件组合查询
     * 翻页查询供应商信息
     * @param pyl  偏移量
     * @param page_size 每页显示条数
     * @return
     */
    public List<Provider> findProviderList(
            @Param("pyl") Integer pyl,
            @Param("page_size") Integer page_size,
            @Param("proName")String proName,
            @Param("proDesc")String proDesc
    );

    /**
     * 获得总条数
     * @return
     */
    public Integer findProviderListCount(
            @Param("proName") String proName,
            @Param("proDesc")String proDesc
    );


    /**
     * 根据ID  获得供应商对象
     * @param id
     * @return
     */
    public Provider findProviderById(Integer id);



}
