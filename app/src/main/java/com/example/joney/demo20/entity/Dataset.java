package com.example.joney.demo20.entity;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Index;
import org.greenrobot.greendao.annotation.Generated;

/**
 * Created by joney on 2018/1/25.
 */
@Entity
public class Dataset {
    @Id(autoincrement = false)      //id自增长
    private Long measureId;        //序列号
    @Index(unique = false)         //唯一性
    private String  Time;          //时间
    private String  NAME;          //名称
    private String  VIS;           //粘度
    private String  TEM;           //温度
    private String  TOR;           //扭矩
    private String  Vlo;           //速度
    private String  Spi;           //转子号
    @Generated(hash = 1817232081)
    public Dataset(Long measureId, String Time, String NAME, String VIS, String TEM,
            String TOR, String Vlo, String Spi) {
        this.measureId = measureId;
        this.Time = Time;
        this.NAME = NAME;
        this.VIS = VIS;
        this.TEM = TEM;
        this.TOR = TOR;
        this.Vlo = Vlo;
        this.Spi = Spi;
    }
    @Generated(hash = 120933356)
    public Dataset() {
    }
    public Long getMeasureId() {
        return this.measureId;
    }
    public void setMeasureId(Long measureId) {
        this.measureId = measureId;
    }
    public String getVIS() {
        return this.VIS;
    }
    public void setVIS(String VIS) {
        this.VIS = VIS;
    }
    public String getTEM() {
        return this.TEM;
    }
    public void setTEM(String TEM) {
        this.TEM = TEM;
    }
    public String getTOR() {
        return this.TOR;
    }
    public void setTOR(String TOR) {
        this.TOR = TOR;
    }
    public String getNAME() {
        return this.NAME;
    }
    public void setNAME(String NAME) {
        this.NAME = NAME;
    }
    public String getVlo()  {
        return this.Vlo;
    }
    public void setVlo(String Vlo) {
        this.Vlo = Vlo;
    }
    public String getSpi() {
        return this.Spi;
    }
    public void setSpi(String Spi) {
        this.Spi = Spi;
    }
    public String getTime() {
        return this.Time;
    }
    public void setTime(String Time) {
        this.Time = Time;
    }

    }
