public class Patient {
    private String id;
    private int ticketNumber;
    private Doctor doctor;

    public Patient(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public int getTicketNumber() {
        return this.ticketNumber;
    }

    public void setTicketNumber(int ticketNumber) {
        this.ticketNumber = ticketNumber;
    }

    public Doctor getDoctor() {
        return doctor;
    }

    public void setDoctor(Doctor doctor) {
        this.doctor = doctor;
    }
}
