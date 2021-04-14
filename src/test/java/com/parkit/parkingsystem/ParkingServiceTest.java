package com.parkit.parkingsystem;

import com.parkit.parkingsystem.constants.ParkingType;
import com.parkit.parkingsystem.dao.ParkingSpotDAO;
import com.parkit.parkingsystem.dao.TicketDAO;
import com.parkit.parkingsystem.model.ParkingSpot;
import com.parkit.parkingsystem.model.Ticket;
import com.parkit.parkingsystem.service.FareCalculatorService;
import com.parkit.parkingsystem.service.ParkingService;
import com.parkit.parkingsystem.util.InputReaderUtil;
import junit.framework.Assert;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.internal.bytebuddy.matcher.ElementMatchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)

public class ParkingServiceTest {

    private static ParkingService parkingService;

    @Mock
    private static InputReaderUtil inputReaderUtil;
    @Mock
    private static ParkingSpotDAO parkingSpotDAO;
    @Mock
    private static TicketDAO ticketDAO;

    private ParkingSpot parkingSpot;

    private FareCalculatorService fareCalculatorService;

    @BeforeEach
    private void setUpPerTest() {
        try {
            when(inputReaderUtil.readVehicleRegistrationNumber()).thenReturn("ABCDEF");
            when(inputReaderUtil.readSelection()).thenReturn(1);

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
    public void checkIfTheSpotIsAvailable(){

        parkingService.processIncomingVehicle();
        when(parkingSpotDAO.getNextAvailableSlot(any(ParkingType.class))).thenReturn(1);
    }

    public void checkIfNextAvailableSlotThrowsExceptionWhenParkingTypeIsNull() throws NullPointerException{

        parkingService.processIncomingVehicle();
        parkingSpotDAO.getNextAvailableSlot(null);
        when(parkingSpotDAO.getNextAvailableSlot(null)).thenThrow(Exception.class);
    }

    public void checkIfUpdateParkingThrowsExceptionWhenParkingSpotIsNull() throws NullPointerException{

        ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR, true);

        when(parkingSpotDAO.updateParking(parkingSpot)).thenThrow(new RuntimeException());
        parkingService.processIncomingVehicle();

        Ticket ticket = new Ticket();
        ticket.setParkingSpot(parkingSpot);
        ticket.setVehicleRegNumber("ABCDEF");
        ticket.setPrice(0);
        ticket.setInTime(new Date());
        ticket.setOutTime(null);
        
        verify(ticketDAO, times(1)).saveTicket(ticket);
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

    public void throw_illegal_argument_exception_when_parking_Spot_is_null(){
        when(inputReaderUtil.readSelection()).thenReturn(1);
        when(parkingSpotDAO.getNextAvailableSlot(ParkingType.CAR)).thenReturn(0);
        parkingService.processIncomingVehicle();

        Throwable exception = assertThrows(Exception.class, () -> parkingService.getNextParkingNumberIfAvailable());
        assertThat(exception.getMessage()).isEqualTo("Error fetching parking number from DB. Parking slots might be full");
    }

    public void throw_illegal_argument_exception_when_user_input_is_0(){
        when(inputReaderUtil.readSelection()).thenReturn(0);
        when(parkingSpotDAO.getNextAvailableSlot(ParkingType.CAR)).thenReturn(0);

        Throwable exception = assertThrows(Exception.class, () -> parkingService.getNextParkingNumberIfAvailable());
        assertThat(exception.getMessage()).isEqualTo("Error fetching parking number from DB. Parking slots might be full");
    }

    @Test
    public void checkUpdateParking() throws NullPointerException{

        ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR, true);
        parkingService.processIncomingVehicle();
        Assert.assertEquals(true, parkingSpotDAO.updateParking(parkingSpot));
    }

    @Test
    public void checkIfSaveTicketThrowsExceptionWhenNull() throws NullPointerException{
        parkingService.processIncomingVehicle();
        ticketDAO.saveTicket(null);
    }

    @Test
    public void getNextParkingNumberIfAvailable() {
        ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR, true);
        parkingService.processIncomingVehicle();
        assertEquals(parkingSpot, parkingService.getNextParkingNumberIfAvailable());
    }

    @Test
    public void getNextParkingNumberIfAvailableThrowsException() {
        ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR, true);
        parkingService.processIncomingVehicle();
        parkingService.getNextParkingNumberIfAvailable().setParkingType(null);
    }

    @Test
    public void parkingSpotIfElse() {
        parkingService.processIncomingVehicle();
        parkingService.getNextParkingNumberIfAvailable();
        when(parkingSpotDAO.getNextAvailableSlot(ParkingType.CAR)).thenReturn(1);

        assertEquals(1, parkingSpotDAO.getNextAvailableSlot(ParkingType.CAR));
    }



}

