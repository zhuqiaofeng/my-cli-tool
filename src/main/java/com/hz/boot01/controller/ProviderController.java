package com.hz.boot01.controller;

import com.hz.boot01.pojo.Provider;
import com.hz.boot01.service.ProviderService;
import com.hz.boot01.utils.JsonMassage;
import com.hz.boot01.utils.RedisUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/provider")
public class ProviderController {
    @Autowired
    private ProviderService providerService;

    @Autowired
    private RedisUtil redisUtil;


    /**
     * VUE 调用 分页查询  多条件
     */
    @RequestMapping(value = "/findProviderList1",method = RequestMethod.GET)
    @ResponseBody
    public JsonMassage<List<Provider>> findProviderList1(
            @RequestParam(value = "pageNo",defaultValue = "1") Integer pageNo,
            @RequestParam(value = "pageSize",defaultValue = "10") Integer pageSize,
            String proName,
            String proDesc
    ){
        List<Provider> list = providerService.findProviderList(pageNo,pageSize,proName,proDesc);
        Integer count = providerService.findProviderListCount(proName,proDesc);

        JsonMassage<List<Provider>> jsonMassage = new JsonMassage<>();
        jsonMassage.setCode(200);
        jsonMassage.setMsg("ok");
        jsonMassage.setCount(count);
        jsonMassage.setData(list);
        return jsonMassage;
    }
    @RequestMapping(value = "/login",method = RequestMethod.POST)
    @ResponseBody
    public JsonMassage<Integer> login(String username,String password){

        System.out.println("username===="+username);
        System.out.println("password===="+password);


        return new JsonMassage<>(200,"请求成功",0,1001);
    }


    @RequestMapping(value = "/saveProvider",method = RequestMethod.POST)
    @ResponseBody
    public JsonMassage<Void> saveProvider(Provider provider){
        providerService.saveProvider(provider);
        return new JsonMassage<>(200,"请求成功",0,null);
    }

    /**
     * 分页查询  多条件
     * @param page  当前页
     * @param limit1 每页显示条数
     */
    @RequestMapping(value = "/findProviderList",method = RequestMethod.GET)
    @ResponseBody
    public JsonMassage<List<Provider>> findProviderList(
            @RequestParam(value = "page",defaultValue = "1") Integer page,
            @RequestParam(value = "limit1",defaultValue = "10") Integer limit1,
             String proName,
             String proDesc
            ){
        List<Provider> list = providerService.findProviderList(page,limit1,proName,proDesc);
        Integer count = providerService.findProviderListCount(proName,proDesc);

        JsonMassage<List<Provider>> jsonMassage = new JsonMassage<>();
        jsonMassage.setCode(0);
        jsonMassage.setMsg("请求成功");
        jsonMassage.setCount(count);
        jsonMassage.setData(list);
        return jsonMassage;
    }

    @RequestMapping("/findProviderById1/{id}")
    @ResponseBody
    public JsonMassage<Provider> findProviderById1(@PathVariable("id") Integer id){
        Provider provider = providerService.findProviderById(id);

        return new JsonMassage<>(200,"请求成功",0,provider);
    }


    @RequestMapping("/findProviderById/{id}")
    public String findProviderById(@PathVariable("id") Integer id, Model model){

        model.addAttribute("pro",providerService.findProviderById(id));
        return "provider_edit";
    }


    @RequestMapping(value = "/deleteProviderById",method = RequestMethod.POST)
    @ResponseBody
    public JsonMassage<Void> deleteProviderById(Integer id){
        providerService.deleteProviderById(id);
        return new JsonMassage<>(200,"请求成功",0,null);
    }

    @RequestMapping(value = "/updateProvider",method = RequestMethod.POST)
    @ResponseBody
    public JsonMassage<Void> updateProvider(Provider provider){
        providerService.updateProvider(provider);
        return new JsonMassage<>(200,"请求成功",0,null);
    }

}
