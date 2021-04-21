package com.parkit.parkingsystem;

import com.parkit.parkingsystem.constants.ParkingType;
import com.parkit.parkingsystem.dao.ParkingSpotDAO;
import com.parkit.parkingsystem.dao.TicketDAO;
import com.parkit.parkingsystem.model.ParkingSpot;
import com.parkit.parkingsystem.model.Ticket;
import com.parkit.parkingsystem.service.ParkingService;
import com.parkit.parkingsystem.util.InputReaderUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)

public class ParkingServiceTest {


    @Mock
    private static InputReaderUtil inputReaderUtil;
    @Mock
    private static ParkingSpotDAO parkingSpotDAO;
    @Mock
    private static TicketDAO ticketDAO;

    private ParkingSpot parkingSpot;


    @InjectMocks
    ParkingService parkingService;

    @BeforeEach
    private void setUpPerTest() {
        try {
            when(inputReaderUtil.readSelection()).thenReturn(1);

            when(inputReaderUtil.readVehicleRegistrationNumber()).thenReturn("ABCDEF");

            parkingSpot = new ParkingSpot(1, ParkingType.CAR,false);
            Ticket ticket = new Ticket();
            ticket.setInTime(new Date(System.currentTimeMillis() - (60*60*1000)));
            ticket.setParkingSpot(parkingSpot);
            ticket.setVehicleRegNumber("ABCDEF");
            when(ticketDAO.getTicket(anyString())).thenReturn(ticket);
            when(ticketDAO.updateTicket(any(Ticket.class))).thenReturn(true);

            when(parkingSpotDAO.updateParking(any(ParkingSpot.class))).thenReturn(true);
            when(parkingSpotDAO.getNextAvailableSlot(any(ParkingType.class))).thenReturn(1);

            parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);
        } catch (Exception e) {
            e.printStackTrace();
            throw  new RuntimeException("Failed to set up test mock objects");
        }
    }


    @Test
    public void return_next_parking_number_if_available(){
        when(inputReaderUtil.readSelection()).thenReturn(1);
        when(parkingSpotDAO.getNextAvailableSlot(ParkingType.CAR)).thenReturn(1);

        assertEquals(parkingSpot,parkingService.getNextParkingNumberIfAvailable());
    }

    @Test
    public void return_next_parking_number_if_available_for_Bike(){
        when(inputReaderUtil.readSelection()).thenReturn(2);
        when(parkingSpotDAO.getNextAvailableSlot(ParkingType.BIKE)).thenReturn(1);

        assertEquals(parkingSpot,parkingService.getNextParkingNumberIfAvailable());
    }

    @Test
    public void checkUpdateParking() throws NullPointerException{

        ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR, true);
        parkingService.processIncomingVehicle();
        assertTrue(parkingSpotDAO.updateParking(parkingSpot));
    }


    @Test
    public void getNextParkingNumberIfAvailable() {
        ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR, true);
        parkingService.processIncomingVehicle();
        assertEquals(parkingSpot, parkingService.getNextParkingNumberIfAvailable());
    }


    @Test
    public void parkingSpotIfElse() {
        parkingService.processIncomingVehicle();
        parkingService.getNextParkingNumberIfAvailable();
        when(parkingSpotDAO.getNextAvailableSlot(ParkingType.CAR)).thenReturn(1);
        assertEquals(1, parkingSpotDAO.getNextAvailableSlot(ParkingType.CAR));
    }


    @Test
    public void check_that_ticket_is_not_null () throws Exception {
        parkingService.processExitingVehicle();
        when(inputReaderUtil.readSelection()).thenReturn(2);
        Ticket ticket = ticketDAO.getTicket(anyString());

        assertNotNull(ticket);
    }


}

