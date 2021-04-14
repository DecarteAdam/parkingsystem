package com.parkit.parkingsystem.service;

import com.parkit.parkingsystem.config.DataBaseConfig;
import com.parkit.parkingsystem.constants.Fare;
import com.parkit.parkingsystem.model.Ticket;

import java.text.DecimalFormat;
import java.util.Date;

public class FareCalculatorService {
    private static DecimalFormat df = new DecimalFormat("0.00");
    public DataBaseConfig dataBaseConfig = new DataBaseConfig();
    int freeTime =  30;
    double discount = 0.95;


    public void calculateFare(Ticket ticket, boolean recurringUser) {
        if( (ticket.getOutTime() == null) || (ticket.getOutTime().before(ticket.getInTime())) ){
            throw new IllegalArgumentException("Out time provided is incorrect:"+ticket.getOutTime().toString());
        }

        /*boolean recurringUser;*/

        Date inHour = ticket.getInTime();
        Date outHour = ticket.getOutTime();

        //TODO: Some tests are failing here. Need to check if this logic is correct
         long timeDifferenceInMinutes = ((outHour.getTime() - inHour.getTime())/1000)/60;
       // double duration = count / 60.0;

        /*
         If user parking time is less the 30 minutes, the parking cost is free.
          else he pay the regular price
         */

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

    /*public void calculateDiscount() throws SQLException, ClassNotFoundException {
        Connection con = dataBaseConfig.getConnection();
        PreparedStatement ps = con.prepareStatement(DBConstants.GET_EXISTING_VEHICULE);
        ResultSet rs = ps.executeQuery();
        rs.next();
        if (rs.)
    }*/
}