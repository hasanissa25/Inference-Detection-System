package com.example.demo.data.model;

import javax.persistence.*;

@Entity
public class GeneralSequenceNumber {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "gen_generator")
    @SequenceGenerator(name="gen_generator", sequenceName = "gen_seq")
    protected Long number;


}
