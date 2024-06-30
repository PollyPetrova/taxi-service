package org.example.taxiservice.entity;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@DiscriminatorValue("PASSENGER")
public class Passenger extends User {

    private String paymentMethod;

}
