package com.fit2cloud.config.model;

import io.swagger.annotations.ApiModelProperty;
import java.io.Serializable;

public class Demo implements Serializable {
    @ApiModelProperty("ID")
    private String id;

    @ApiModelProperty("名称")
    private String name;

    @ApiModelProperty("创建时间")
    private Long createTime;

    /**
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database table demo
     *
     * @mbg.generated
     */
    private static final long serialVersionUID = 1L;

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column demo.id
     *
     * @return the value of demo.id
     *
     * @mbg.generated
     */
    public String getId() {
        return id;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column demo.id
     *
     * @param id the value for demo.id
     *
     * @mbg.generated
     */
    public void setId(String id) {
        this.id = id == null ? null : id.trim();
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column demo.name
     *
     * @return the value of demo.name
     *
     * @mbg.generated
     */
    public String getName() {
        return name;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column demo.name
     *
     * @param name the value for demo.name
     *
     * @mbg.generated
     */
    public void setName(String name) {
        this.name = name == null ? null : name.trim();
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column demo.create_time
     *
     * @return the value of demo.create_time
     *
     * @mbg.generated
     */
    public Long getCreateTime() {
        return createTime;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column demo.create_time
     *
     * @param createTime the value for demo.create_time
     *
     * @mbg.generated
     */
    public void setCreateTime(Long createTime) {
        this.createTime = createTime;
    }
}