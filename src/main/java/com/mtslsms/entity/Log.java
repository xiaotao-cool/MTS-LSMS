package com.mtslsms.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "log")
public class Log {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private String terminalId;
    private String terminalType;
    private String ip;
    private LocalDateTime time;
    private String type;
    private String content;

    public  Log() {
    }
    public Log(Integer id, String terminalId, String terminalType, String ip, LocalDateTime time, String type, String content) {
        this.id = id;
        this.terminalId = terminalId;
        this.terminalType = terminalType;
        this.ip = ip;
        this.time = time;
        this.type = type;
        this.content = content;
    }

    public Integer getId() {return id;}
    public void setId(Integer id) {this.id = id;}

    public String getTerminalId() {return terminalId;}
    public void setTerminalId(String terminalId) {this.terminalId = terminalId;}

    public String getTerminalType() {return terminalType;}
    public void setTerminalType(String terminalType) {this.terminalType = terminalType;}

    public String getIp() {return ip;}
    public void setIp(String ip) {this.ip = ip;}

    public LocalDateTime getTime() {return time;}
    public void setTime(LocalDateTime time) {this.time = time;}

    public String getType() {return type;}
    public void setType(String type) {this.type = type;}

    public String getContent() {return content;}
    public void setContent(String content) {this.content = content;}

}
