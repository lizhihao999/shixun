
package cn.wolfcode.web.modules.custinfo.controller;

import cn.afterturn.easypoi.excel.ExcelExportUtil;
import cn.afterturn.easypoi.excel.ExcelImportUtil;
import cn.afterturn.easypoi.excel.entity.ExportParams;
import cn.afterturn.easypoi.excel.entity.ImportParams;
import cn.afterturn.easypoi.excel.entity.result.ExcelImportResult;
import cn.wolfcode.web.commons.entity.CodeMsg;
import cn.wolfcode.web.commons.entity.ExcelExportEntityWrapper;
import cn.wolfcode.web.commons.entity.LayuiPage;
import cn.wolfcode.web.commons.utils.*;
import cn.wolfcode.web.modules.BaseController;
import cn.wolfcode.web.modules.custinfo.entity.TbCustomer;
import cn.wolfcode.web.modules.custinfo.service.ITbCustomerService;
import cn.wolfcode.web.modules.log.LogModules;
import cn.wolfcode.web.modules.sys.entity.SysUser;
import cn.wolfcode.web.modules.sys.entity.SysUserVerifyEntity;
import cn.wolfcode.web.modules.sys.form.LoginForm;
import cn.wolfcode.web.modules.sys.service.SysUserService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import com.sun.corba.se.spi.orbutil.threadpool.Work;
import com.sun.org.apache.xpath.internal.operations.Mod;
import link.ahsj.core.annotations.*;
import link.ahsj.core.entitys.ApiModel;
import link.ahsj.core.entitys.KeyValue;
import link.ahsj.core.exception.AppServerException;
import link.ahsj.core.exception.ErrorCode;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.xmlbeans.impl.jam.JParameter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.util.CollectionUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.UnsupportedEncodingException;
import java.rmi.server.ExportException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * @author 叶师傅
 * @since 2024-06-01
 */
@Controller
@RequestMapping("custinfo")
public class TbCustomerController extends BaseController {

    @Autowired
    private ITbCustomerService entityService;

    @Autowired
    private SysUserService sysUserService;

    private static final String LogModule = "TbCustomer";

    @GetMapping("/list.html")
    public ModelAndView list(ModelAndView mv) {
        List<KeyValue> citys = CityUtils.citys;

        mv.addObject("citys", citys);

        mv.setViewName("cust/custinfo/list");
        return mv;
    }

    @RequestMapping("/add.html")
    @PreAuthorize("hasAuthority('cust:custinfo:add')")
    public ModelAndView toAdd(ModelAndView mv) {
        List<KeyValue> citys = CityUtils.citys;
        mv.addObject("citys", citys);


        mv.setViewName("cust/custinfo/add");
        return mv;
    }

    @GetMapping("/{id}.html")
    @PreAuthorize("hasAuthority('cust:custinfo:update')")
    public ModelAndView toUpdate(@PathVariable("id") String id, ModelAndView mv) {
        List<KeyValue> citys = CityUtils.citys;
        mv.addObject("citys", citys);
        mv.setViewName("cust/custinfo/update");
        mv.addObject("obj", entityService.getById(id));
        mv.addObject("id", id);
        return mv;
    }

    @RequestMapping("list")
    @PreAuthorize("hasAuthority('cust:custinfo:list')")
    public ResponseEntity page(LayuiPage layuiPage, String parameterName, String province, String openStatus) {

        SystemCheckUtils.getInstance().checkMaxPage(layuiPage);


        IPage page = new Page<>(layuiPage.getPage(), layuiPage.getLimit());

        page = entityService.lambdaQuery()

                .like(!StringUtils.isEmpty(parameterName), TbCustomer::getCustomerName, parameterName)
                .or()
                .like(!StringUtils.isEmpty(parameterName), TbCustomer::getLegalLeader, parameterName)
                .eq(!StringUtils.isEmpty(province), TbCustomer::getProvince, province)
                .eq(!StringUtils.isEmpty(openStatus), TbCustomer::getOpenStatus, openStatus)
                .page(page);
        //得到分页中的列表
        List<TbCustomer> customerList = page.getRecords();
        for (TbCustomer customer : customerList) {
            if (customer != null) {
                String cityValue = CityUtils.getCityValue(customer.getProvince());
                customer.setProvinceName(cityValue);

            }
            //录入人
            if (customer.getInputUserId() != null) {
                SysUser sysUser = sysUserService.getById(customer.getInputUserId());
                if (sysUser != null) {
                    customer.setInputUserName(sysUser.getUsername());
                }
            }


        }
        return ResponseEntity.ok(LayuiTools.toLayuiTableModel(page));

    }

    @SameUrlData
    @PostMapping("save")
    @SysLog(value = LogModules.SAVE, module = LogModule)
    @PreAuthorize("hasAuthority('cust:custinfo:add')")
    public ResponseEntity<ApiModel> save(@Validated({AddGroup.class})
                                         @RequestBody TbCustomer entity,
                                         HttpServletRequest request) {
        //录入时间,获取当前时间
        entity.setInputTime(LocalDateTime.now());
        //录入人
        SysUser sysUser = (SysUser) request.getSession().getAttribute(LoginForm.LOGIN_USER_KEY);
        //赋值
        entity.setInputUserId(sysUser.getUserId());

        Integer count = entityService.lambdaQuery().
                eq(!StringUtils.isEmpty(entity.getCustomerName()), TbCustomer::getCustomerName, entity.getCustomerName())
                .count();

        if (count > 0) {
            throw new AppServerException("企业名称已经重复了");
        }
        entityService.save(entity);
        return ResponseEntity.ok(ApiModel.ok());
    }

    @SameUrlData
    @SysLog(value = LogModules.UPDATE, module = LogModule)
    @PutMapping("update")
    @PreAuthorize("hasAuthority('cust:custinfo:update')")
    public ResponseEntity<ApiModel> update(@Validated({UpdateGroup.class}) @RequestBody TbCustomer entity) {
        Integer count = entityService.lambdaQuery().
                eq(!StringUtils.isEmpty(entity.getCustomerName()), TbCustomer::getCustomerName, entity.getCustomerName())
                .ne(TbCustomer::getId, entity.getId())
                .count();

        if (count > 0) {
            throw new AppServerException("企业名称已经重复了");
        }

        entityService.updateById(entity);
        return ResponseEntity.ok(ApiModel.ok());
    }

    @SysLog(value = LogModules.DELETE, module = LogModule)
    @DeleteMapping("delete/{id}")
    @PreAuthorize("hasAuthority('cust:custinfo:delete')")
    public ResponseEntity<ApiModel> delete(@PathVariable("id") String id) {
        entityService.removeById(id);
        return ResponseEntity.ok(ApiModel.ok());
    }

    @RequestMapping("/export")
    public void export(HttpServletResponse response,
                       String parameterName,
                       String province,
                       String openStatus)

            throws UnsupportedEncodingException {
//        System.out.println("进入后台看吗？");

        //1.准备你需要导出的数据
        List<TbCustomer> tbCustomerList = entityService.lambdaQuery()
                .like(!StringUtils.isEmpty(parameterName), TbCustomer::getCustomerName, parameterName)
                .or()
                .like(!StringUtils.isEmpty(parameterName), TbCustomer::getLegalLeader, parameterName)
                .eq(!StringUtils.isEmpty(openStatus), TbCustomer::getOpenStatus, openStatus)
                .eq(!StringUtils.isEmpty(province), TbCustomer::getProvince, province)
                .list();

        //设置属性
        ExportParams exportParams = new ExportParams();

        //2.封装成workbook
        /**
         * 参数一：表格属性
         * 参数二：你需要导出的实体类的字节码 需要配合dExcel注释使用
         * 参数三：你需要导出的数据
         */


        Workbook workbook = ExcelExportUtil.exportExcel
                (exportParams, TbCustomer.class, tbCustomerList);
        //3.导出
        PoiExportHelper.exportExcel(response, "企业客户管理", workbook);
    }

    @GetMapping("import.html")
    public ModelAndView toImport(ModelAndView mv) {
        mv.setViewName("cust/custinfo/importUser");
        return mv;
    }

    @GetMapping("template")
    public void template(HttpServletResponse response) throws UnsupportedEncodingException {
        ExcelExportEntityWrapper wrapper = new ExcelExportEntityWrapper();
        wrapper.entity(TbCustomer.CUSTOMERNAME_COL, "customername", 30);
        wrapper.entity(TbCustomer.LEGAILEADER_COL, "legalLeader", 30);

        Workbook workbook = ExcelExportUtil.exportExcel(
                new ExportParams(),
                wrapper.getResult(),
                new ArrayList<>()
        );
        PoiExportHelper.exportExcel(response, "企业客户管理模板", workbook);
    }

    @PostMapping("import")

    public ResponseEntity importUser(MultipartFile file) throws Exception {

        ImportParams params = PoiImportHelper.buildImportParams(
                new String[]{
                        TbCustomer.CUSTOMERNAME_COL,
                        TbCustomer.LEGAILEADER_COL
                },new Class[]{ImportGroup.class});

        ExcelImportResult result = ExcelImportUtil.importExcelMore(
                file.getInputStream(),
                TbCustomer.class, params);

        entityService.saveBatch(result.getList());

        return ResponseEntity.ok(ApiModel.ok());
    }
}
