package com.example.demo.data.model;

import javax.persistence.*;
import java.util.List;

@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "Entity")
public abstract class SuperTable {

    @Id
    @Column(name = "id", updatable = false, nullable = false)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "table_generator")
    @SequenceGenerator(name="table_generator", sequenceName = "table_seq")
    protected Long id;


    public abstract String getTableName();

    public abstract String[] getColumnNames();

    public abstract String getColumnValue(String col);

    public abstract String getId();


    public abstract void setInference(boolean b);

    public abstract void setByColumn(String column, String val);
}