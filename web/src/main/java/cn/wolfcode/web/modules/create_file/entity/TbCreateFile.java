package cn.wolfcode.web.modules.create_file.entity;

import java.io.Serializable;

/**
 * <p>
 * 
 * </p>
 *
 * @author 李志豪
 * @since 2024-06-02
 */
public class TbCreateFile implements Serializable {

    private static final long serialVersionUID = 1L;

    private Integer id;

    private String name;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "TbCreateFile{" +
            "id=" + id +
            ", name=" + name +
        "}";
    }
}
