package com.parkit.parkingsystem.integration;

import com.parkit.parkingsystem.constants.DBConstants;
import com.parkit.parkingsystem.constants.ParkingType;
import com.parkit.parkingsystem.dao.ParkingSpotDAO;
import com.parkit.parkingsystem.dao.TicketDAO;
import com.parkit.parkingsystem.integration.config.DataBaseTestConfig;
import com.parkit.parkingsystem.integration.service.DataBasePrepareService;
import com.parkit.parkingsystem.model.ParkingSpot;
import com.parkit.parkingsystem.model.Ticket;
import com.parkit.parkingsystem.service.ParkingService;
import com.parkit.parkingsystem.util.InputReaderUtil;
import org.junit.jupiter.api.*;
import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.Connection;
import java.sql.PreparedStatement;

import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ParkingDataBaseIT {

    private static DataBaseTestConfig dataBaseTestConfig = new DataBaseTestConfig();
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

    @AfterAll
    private static void tearDown(){

    }

    @Test
    public void testParkingACar() throws Exception {
        /*int park = parkingSpotDAO.getNextAvailableSlot(ParkingType.CAR);
        System.out.println("First" + park);*/

        ParkingService parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);
        parkingService.processIncomingVehicle();
        //TODO: check that a ticket is actually saved in DB and Parking table is updated with availability

        /*int park2 = parkingSpotDAO.getNextAvailableSlot(ParkingType.CAR);
        System.out.println("Second" + park2);*/


        Ticket actualTicket;
        actualTicket = ticketDAO.getTicket("ABCDEF");



        int result =  parkingSpotDAO.getNextAvailableSlot(ParkingType.CAR) - 1;
        /*ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR, true);*/


        /*boolean parkingSpotD = parkingSpotDAO.updateParking(parkingSpot);
        System.out.println(parkingSpot.isAvailable());
        System.out.println(parkingSpotD);*/

        Ticket expectedTicket = new Ticket();
        expectedTicket.setVehicleRegNumber("ABCDEF");



        Assertions.assertNotNull(actualTicket);

        Assertions.assertEquals(expectedTicket.getVehicleRegNumber(), actualTicket.getVehicleRegNumber());

        Assertions.assertEquals(result-1, 0);

    }

    @Test
    public void testParkingLotExit() throws Exception {
        testParkingACar();
        ParkingService parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);
        parkingService.processExitingVehicle();
        //TODO: check that the fare generated and out time are populated correctly in the database

        Ticket actualTicket;
        actualTicket = ticketDAO.getTicket("ABCDEF");

        Ticket expectedTicket = new Ticket();
        expectedTicket.setVehicleRegNumber("ABCDEF");

        System.out.println("expected" + expectedTicket.getOutTime().toString());
        System.out.println("actual" + actualTicket.getOutTime().toString());

        //Assertions.assertNotNull(actualTicket);

        System.out.println("expected" + expectedTicket.getPrice());
        System.out.println("actual" + actualTicket.getPrice());

        //Assertions.assertEquals(expectedTicket.getPrice(), actualTicket.getPrice());
        //Assertions.assertEquals(expectedTicket.getOutTime().getTime(), actualTicket.getOutTime().getTime());

    }

}
