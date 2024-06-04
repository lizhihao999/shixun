package cn.wolfcode.web.modules.linkman.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;

import java.time.LocalDateTime;
import java.io.Serializable;

/**
 * <p>
 * 客户联系人
 * </p>
 *
 * @author 李四
 * @since 2024-06-01
 */
@Data
public class TbCustLinkman implements Serializable {

    private static final long serialVersionUID = 1L;

    private String id;

    /**
     * 客户id
     */
    private String custId;

//    客户名称
    @TableField(exist = false)
    private String custName;

    /**
     * 联系人名字
     */
    private String linkman;

    /**
     * 性别 1 男 0 女
     */
    private Integer sex;

    /**
     * 年龄
     */
    private Integer age;

    /**
     * 联系人电话
     */
    private String phone;

    /**
     * 职位
     */
    private String position;

    /**
     * 部门
     */
    private String department;

    /**
     * 备注信息
     */
    private String remark;

    /**
     * 录入人
     */
    private String inputUser;

    /**
     * 录入时间
     */
    private LocalDateTime inputTime;

}
