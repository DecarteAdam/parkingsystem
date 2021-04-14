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
import org.apache.commons.lang.time.DateUtils;
import org.junit.jupiter.api.*;
import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.Calendar;
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

    /*@AfterEach
    private void cleanBaseAterEachTest(){
        dataBasePrepareService.clearDataBaseEntries();
    }

    @AfterAll
    private static void tearDown(){

    }*/

    @Test
    public void testParkingACar() throws Exception {
        ParkingService parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);
        parkingService.processIncomingVehicle();
        //TODO: check that a ticket is actually saved in DB and Parking table is updated with availability


        DataBaseTestConfig dataBaseConfig = new DataBaseTestConfig();
        Connection con = dataBaseConfig.getConnection();
        PreparedStatement ps = con.prepareStatement(DBConstants.GET_UPDATED_SPOT);
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
        //testParkingACar();
        ParkingService parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);
        parkingService.processIncomingVehicle();
        parkingService.processExitingVehicle();
        Date expectedDate = new Date();
        //TODO: check that the fare generated and out time are populated correctly in the database

        Ticket actualTicket;
        actualTicket = ticketDAO.getTicket("ABCDEF");

        expectedTicket.setPrice(0);


        Connection con = dataBaseTestConfig.getConnection();
        PreparedStatement ps = con.prepareStatement(DBConstants.GET_OUT_TIME);
        ResultSet rs = ps.executeQuery();

        rs.next();
        Date actualDate = rs.getDate(1);

        Assertions.assertEquals(expectedTicket.getPrice(), actualTicket.getPrice());
        Assertions.assertEquals(expectedDate, actualDate);

    }

}
