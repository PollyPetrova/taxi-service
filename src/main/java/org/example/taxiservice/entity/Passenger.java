package org.example.taxiservice.entity;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "tb_passenger")
@DiscriminatorValue("PASSENGER")
public class Passenger extends User {

    private String paymentMethod;

}
