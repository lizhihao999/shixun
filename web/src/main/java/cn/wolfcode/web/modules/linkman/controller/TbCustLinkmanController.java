package cn.wolfcode.web.modules.linkman.controller;

import cn.wolfcode.web.commons.entity.LayuiPage;
import cn.wolfcode.web.commons.utils.LayuiTools;
import cn.wolfcode.web.commons.utils.SystemCheckUtils;
import cn.wolfcode.web.modules.BaseController;
import cn.wolfcode.web.modules.custinfo.entity.TbCustomer;
import cn.wolfcode.web.modules.custinfo.service.ITbCustomerService;
import cn.wolfcode.web.modules.linkman.entity.TbCustLinkman;
import cn.wolfcode.web.modules.linkman.service.ITbCustLinkmanService;
import cn.wolfcode.web.modules.log.LogModules;
import cn.wolfcode.web.modules.sys.entity.SysUser;
import cn.wolfcode.web.modules.sys.form.LoginForm;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import link.ahsj.core.annotations.AddGroup;
import link.ahsj.core.annotations.SameUrlData;
import link.ahsj.core.annotations.SysLog;
import link.ahsj.core.annotations.UpdateGroup;
import link.ahsj.core.entitys.ApiModel;
import org.apache.poi.util.StringUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.List;

/**
 * @author 李四
 * @since 2024-06-01
 */
@Controller
@RequestMapping("linkman")
public class TbCustLinkmanController extends BaseController {

    @Autowired
    private ITbCustLinkmanService entityService;

    @Autowired
    private ITbCustomerService customerService;

    private static final String LogModule = "TbCustLinkman";

    @GetMapping("/list.html")
    public ModelAndView list(ModelAndView mv) {

        List<TbCustomer> tbCustomerList = customerService.list();
        mv.addObject("tbCustomerList",tbCustomerList);
        mv.setViewName("user/linkman/list");
        return mv;
    }

    @RequestMapping("/add.html")
    @PreAuthorize("hasAuthority('user:linkman:add')")
    public ModelAndView toAdd(ModelAndView mv) {
        List<TbCustomer> tbCustomerList = customerService.list();
        mv.addObject("tbCustomerList",tbCustomerList);


        mv.setViewName("user/linkman/add");
        return mv;
    }

    @GetMapping("/{id}.html")
    @PreAuthorize("hasAuthority('user:linkman:update')")
    public ModelAndView toUpdate(@PathVariable("id") String id, ModelAndView mv) {

        List<TbCustomer> tbCustomerList = customerService.list();
        mv.addObject("tbCustomerList",tbCustomerList);

        mv.setViewName("user/linkman/update");
        mv.addObject("obj", entityService.getById(id));
        mv.addObject("id", id);
        return mv;
    }

    @RequestMapping("list")
    @PreAuthorize("hasAuthority('user:linkman:list')")
    public ResponseEntity page(LayuiPage layuiPage,String parameterName,String custId) {
        SystemCheckUtils.getInstance().checkMaxPage(layuiPage);
        IPage page = new Page<>(layuiPage.getPage(), layuiPage.getLimit());

        page=entityService.lambdaQuery()
                .eq(!StringUtils.isEmpty(custId),TbCustLinkman::getCustId,custId)
                .like(!StringUtils.isEmpty(parameterName),TbCustLinkman::getLinkman,parameterName)
                .or()
                .like(!StringUtils.isEmpty(parameterName),TbCustLinkman::getPhone,parameterName)
                .page(page);

        List<TbCustLinkman> records = page.getRecords();
        for (TbCustLinkman custLinkman : records) {
            String custId1 = custLinkman.getCustId();

            TbCustomer tbCustomer = customerService.getById(custId);
            if (tbCustomer!=null){
                custLinkman.setCustName(tbCustomer.getCustomerName());
            }
        }


        return ResponseEntity.ok(LayuiTools.toLayuiTableModel(page));
    }

    @SameUrlData
    @PostMapping("save")
    @SysLog(value = LogModules.SAVE, module =LogModule)
    @PreAuthorize("hasAuthority('user:linkman:add')")
    public ResponseEntity<ApiModel> save(@Validated({AddGroup.class})
                                             @RequestBody TbCustLinkman entity,
                                         HttpServletRequest request
    ) {
        entity.setInputTime(LocalDateTime.now());
        SysUser sysUser = (SysUser)request.getSession().getAttribute(LoginForm.LOGIN_USER_KEY);
        entity.setInputUser(sysUser.getUserId());


        entityService.save(entity);
        return ResponseEntity.ok(ApiModel.ok());
    }

    @SameUrlData
    @SysLog(value = LogModules.UPDATE, module = LogModule)
    @PutMapping("update")
    @PreAuthorize("hasAuthority('user:linkman:update')")
    public ResponseEntity<ApiModel> update(@Validated({UpdateGroup.class}) @RequestBody TbCustLinkman entity) {
        entityService.updateById(entity);
        return ResponseEntity.ok(ApiModel.ok());
    }

    @SysLog(value = LogModules.DELETE, module = LogModule)
    @DeleteMapping("delete/{id}")
    @PreAuthorize("hasAuthority('user:linkman:delete')")
    public ResponseEntity<ApiModel> delete(@PathVariable("id") String id) {
        entityService.removeById(id);
        return ResponseEntity.ok(ApiModel.ok());
    }

    @RequestMapping("listByCustomerId")
    public ResponseEntity<ApiModel> selectCustomerById(String custId){
//        System.out.println(custId);
        //如果ID值为空
        if (StringUtils.isEmpty(custId)){
            return ResponseEntity.ok(ApiModel.ok());
        }

        List<TbCustLinkman> list =
                entityService.
                lambdaQuery()
                .eq(TbCustLinkman::getCustId,custId)
                .list();
        return ResponseEntity.ok(ApiModel.data(list));
    }

}
