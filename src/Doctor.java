public class Doctor {
    public String id;
    public PatientsQueue queueTree;

    public Doctor(String id) {
        this.id = id;
        this.queueTree = new PatientsQueue();
    }

    public String getId() {
        return id;
    }
}
