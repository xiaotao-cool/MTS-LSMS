package com.mtslsms.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "event")
public class Event {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private String level; // HIGH, MEDIUM, WARNING
    private String description;
    private LocalDateTime time;

    public Event() {}
    public Event(Integer id,String level, String description, LocalDateTime time) {
        this.id = id;
        this.level = level;
        this.description = description;
        this.time = time;
    }

    public Integer getId() {return id;}
    public void setId(Integer id) {this.id = id;}

    public String getLevel() {return level;}
    public void setLevel(String level) {this.level = level;}

    public String getDescription() {return description;}
    public void setDescription(String description) {this.description = description;}

    public LocalDateTime getTime() {return time;}
    public void setTime(LocalDateTime time) {this.time = time;}
}