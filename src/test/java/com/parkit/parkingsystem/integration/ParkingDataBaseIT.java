package com.parkit.parkingsystem.integration;

import com.parkit.parkingsystem.constants.DBConstants;
import com.parkit.parkingsystem.dao.ParkingSpotDAO;
import com.parkit.parkingsystem.dao.TicketDAO;
import com.parkit.parkingsystem.integration.config.DataBaseTestConfig;
import com.parkit.parkingsystem.integration.service.DataBasePrepareService;
import com.parkit.parkingsystem.model.Ticket;
import com.parkit.parkingsystem.service.ParkingService;
import com.parkit.parkingsystem.util.InputReaderUtil;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;

import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ParkingDataBaseIT {

    private static DataBaseTestConfig dataBaseTestConfig = new DataBaseTestConfig();
    private static Ticket expectedTicket = new Ticket();
    private static ParkingSpotDAO parkingSpotDAO;
    private static TicketDAO ticketDAO;
    private static DataBasePrepareService dataBasePrepareService;


    @Mock
    private static InputReaderUtil inputReaderUtil;

    @BeforeAll
    private static void setUp() throws Exception{
        parkingSpotDAO = new ParkingSpotDAO();
        parkingSpotDAO.dataBaseConfig = dataBaseTestConfig;
        ticketDAO = new TicketDAO();
        ticketDAO.dataBaseConfig = dataBaseTestConfig;
        dataBasePrepareService = new DataBasePrepareService();
    }

    @BeforeEach
    private void setUpPerTest() throws Exception {
        when(inputReaderUtil.readSelection()).thenReturn(1);
        when(inputReaderUtil.readVehicleRegistrationNumber()).thenReturn("ABCDEF");
        dataBasePrepareService.clearDataBaseEntries();
    }

    @Test
    public void testParkingACar() throws Exception {
        ParkingService parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);
        parkingService.processIncomingVehicle();
        //TODO: check that a ticket is actually saved in DB and Parking table is updated with availability

        Connection con = dataBaseTestConfig.getConnection();
        PreparedStatement ps = con.prepareStatement(DBConstants.GET_UPDATED_SPOT);
        ps.setString(1,"0");
        ResultSet rs = ps.executeQuery();


        rs.next();
        int res = rs.getInt(1);


        Ticket actualTicket;
        actualTicket = ticketDAO.getTicket("ABCDEF");


        expectedTicket.setVehicleRegNumber("ABCDEF");

        Assertions.assertNotNull(actualTicket);


        Assertions.assertEquals(expectedTicket.getVehicleRegNumber(), actualTicket.getVehicleRegNumber());
        Assertions.assertEquals(0, res);

    }

    @Test
    public void testParkingLotExit() throws Exception {
        /*testParkingACar();*/
        ParkingService parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);
        parkingService.processIncomingVehicle();
        Thread.sleep(3000);
        parkingService.processExitingVehicle();
        parkingService.processIncomingVehicle();
        Thread.sleep(3000);
        parkingService.processExitingVehicle();
        Date expectedDate = new Date();
        //TODO: check that the fare generated and out time are populated correctly in the database

        Ticket actualTicket;
        actualTicket = ticketDAO.getTicket("ABCDEF");

        expectedTicket.setPrice(0);


        Connection con = dataBaseTestConfig.getConnection();
        PreparedStatement ps = con.prepareStatement(DBConstants.GET_OUT_TIME);
        ps.setDate(1, new java.sql.Date(expectedDate.getTime()));
        ResultSet rs = ps.executeQuery();

        if (rs.first()) {
            Date actualDate  = rs.getDate(1);

            System.out.println("Expected " + expectedDate);
            System.out.println("Actual " + actualDate);

            Assertions.assertEquals(expectedTicket.getPrice(), actualTicket.getPrice());
            Assertions.assertEquals(expectedDate, actualDate);
        }
    }

    @Test
    public void testDiscountForExistingVehicul() throws SQLException, ClassNotFoundException {
        ParkingService parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);
        parkingService.processIncomingVehicle();

        Connection con = dataBaseTestConfig.getConnection();
        PreparedStatement ps = con.prepareStatement(DBConstants.GET_EXISTING_VEHICULE);
        ps.setString(1,"ABCDEF");

        ResultSet rs = ps.executeQuery();

        rs.next();

        String existingVehicul = rs.getString(1);

        Assertions.assertEquals("ABCDEF", existingVehicul);
    }


}
