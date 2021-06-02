package com.parkit.parkingsystem.service;

import com.parkit.parkingsystem.constants.Fare;
import com.parkit.parkingsystem.model.Ticket;

import java.util.Date;

public class FareCalculatorService {
    int freeTime =  30;
    double discount = 0.95;


    /**
     * Calculate payment fare according to the parking time and vehicle type
     */
    public void calculateFare(Ticket ticket, boolean recurringUser) {
        if( (ticket.getOutTime() == null) || (ticket.getOutTime().before(ticket.getInTime())) ){
            throw new IllegalArgumentException("Out time provided is incorrect:"+ticket.getOutTime().toString());
        }

        Date inHour = ticket.getInTime();
        Date outHour = ticket.getOutTime();

         long timeDifferenceInMinutes = ((outHour.getTime() - inHour.getTime())/1000)/60;

        if (timeDifferenceInMinutes <= freeTime){
            ticket.setPrice(0);
        }else{
            switch (ticket.getParkingSpot().getParkingType()){
                case CAR: {
                    if (recurringUser){
                        ticket.setPrice(((timeDifferenceInMinutes * Fare.CAR_RATE_PER_HOUR) / 60) * discount);
                    }else {
                        ticket.setPrice((timeDifferenceInMinutes * Fare.CAR_RATE_PER_HOUR) / 60);
                    }
                    break;
                }
                case BIKE: {
                    if (recurringUser){
                        ticket.setPrice(((timeDifferenceInMinutes * Fare.BIKE_RATE_PER_HOUR) / 60) * discount);
                    }else{
                        ticket.setPrice((timeDifferenceInMinutes * Fare.BIKE_RATE_PER_HOUR) / 60);
                    }
                    break;
                }
                default: throw new IllegalArgumentException("Unknown Parking Type");
            }
        }


    }
}